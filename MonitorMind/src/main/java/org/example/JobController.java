package org.example;

import model.*;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.print.Doc;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class JobController {

    private ZNodeController zController;
    private DockerController dController;

    private String handlerMonitor;
    private static final long CHUNK_SIZE = 1<<26L;
    private static final long MAX_WORKERS_PER_JOB = 30;
    private static Random random = new Random();
    private Job job;

    public JobController(ZNodeController zc){
        this.zController = zc;
        this.dController = new DockerController();
    }

    /**
     * This method is called via the web server API endpoint /api/job/assign/$id.
     * It starts the process of job handling. First it calculates the number of workers
     * needed for the job. Based on the type of the job the appropriate steps are calculated
     * @param jobNodeId
     * @param forUser
     */
    public void handleJob(String jobNodeId, String forUser, String handlerMonitor) throws Exception, IOException {
        this.handlerMonitor = handlerMonitor;
        JsonObject jobInfoObj = zController.getJobData(forUser+"/"+jobNodeId);
        //Structure Job Flow
        System.out.println("Starting Job Flow Design");

        //Creating the new job local object
        String user = jobInfoObj.getString("user");
        String datasetPath = "uploads/"+user+"/datasets/"+jobInfoObj.getString("dataset_file");

        Job j = new Job(datasetPath, jobInfoObj.getString("executable_file"),forUser+"/"+jobNodeId, Integer.valueOf(jobInfoObj.getString("reducers_num")));
        j.setJobCommands(jobInfoObj.getString("map_method"), jobInfoObj.getString("reduce_method"));

        zController.updateJobStatus(j.getJobZnode(), "planning");

        //Parse File to check for input size
        System.out.println(" > Will check input file size to decide chunking...");
        File file = new File(j.getGlobalInputPath());
        System.out.println(file.getAbsolutePath());

        long fileSize = 0;
        long numOfWorkers = 0;
        long numOfTotalChunks = 0;
        int nMinChunksPerWorker = 10;
        long numOfChunksPerWorker = 0;
        long remainingChunks = 0;

        /**
         * We have max 30 workers for chunking a dataset file in a distributed way.
         * We create tasks of chunking. Each task given to a single worker creates N_min chunks of 64MB if we are under the limit explained below.
         * So with 30 workers we can chunk a file of max N_min*30*64MB size using N_min chunks per worker.
         * If this limit is reached then the number of chunks per worker should be increased and the team will be consisted of exactly 30 workers.
         */

        //Variables used for keeping track of statistics for each stage
        long startTime;
        long endTime;
        double diffTime;
        int tasksForStage;
        
        if (file.exists()) {
            startTime = System.currentTimeMillis();
            fileSize = file.length();
            System.out.println(" > File size in bytes: " + fileSize);
            System.out.println(" > Calculating workers and chunks per worker needed for chunk size: 64 MB.");


            // Max number of workers
            numOfWorkers = (long) fileSize / ((long) CHUNK_SIZE * 10);

            // Redefine the number of workers based on the upper bound of workers.
            numOfWorkers = Math.max(1, Math.min(numOfWorkers, MAX_WORKERS_PER_JOB));

            // The total number of chunks.
            numOfTotalChunks = fileSize / CHUNK_SIZE + (fileSize % CHUNK_SIZE == 0 ? 0 : 1);

            // Calculates the number of chunks per worker.
            numOfChunksPerWorker = numOfTotalChunks / numOfWorkers;

            // The remaining number of chunks.
            remainingChunks = numOfTotalChunks % numOfWorkers;

            long[] chunks_for_i = new long[(int) numOfWorkers];

            // Calculate the number of chunks per worker...
            for (long i=0; i<numOfWorkers; i++) {
                if (i < remainingChunks) {
                    chunks_for_i[(int) i] = numOfChunksPerWorker + 1;
                }
                else {
                    chunks_for_i[(int) i] = numOfChunksPerWorker;
                }
            }

            long[] offsets_for_i = new long[(int) numOfWorkers];
            long sum = 0;

            for (int i=0; i<(int) numOfWorkers; i++) {
                offsets_for_i[i] = sum;
                sum += chunks_for_i[i];
            }

            System.out.println(" > Will deploy "+numOfWorkers+" workers to be used for chunking of the dataset.");
            for (int i=0; i<(int) numOfWorkers; i++) {
                System.out.println("> Worker [" + i + "] will create " + chunks_for_i[i] + " chunks of 64MB totaling: " + chunks_for_i[i] * CHUNK_SIZE + " bytes.");
            }

            // Enqueue Chunk tasks
            for (int w=0; w<numOfWorkers; w++){
                String id = "t"+UUID.randomUUID().toString().replace("-","");
                String tZnodePath = j.getJobZnode() + "/tasks/" + id;
                Task t = new ChunkTask(id,"chunk", "/jobs/"+tZnodePath, TaskType.CHUNK, j.getGlobalInputPath(), chunks_for_i[w], offsets_for_i[w], CHUNK_SIZE);
                zController.insertTaskToZK(t);
                while (true) {
                    String worker = reserveWorker();
                    System.out.println("Trying to assign task: "+ t.getZnodePath()+" to reserved worker: "+worker);
                    if(assignTask(t,worker)){
                        t.setOnWorker(worker);
                        System.out.println("Will insert assignment for task: " + id + " at job:"+j.getJobZnode()+" on worker: "+worker);
                        zController.insertAssignmentToZK(j.getJobZnode(), id, worker);
                        j.enqeueTask(t);
                        break;
                    }
                    Thread.sleep(10);
                    System.out.println("Failed to assign, trying again...");
                }
            }

            zController.updateJobStatus(j.getJobZnode(), "chunking");
            while(true){
                int chunkTasksCompleted = 0;
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    JsonObject data = zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s);
                    if(data.getString("command").equals("chunk") && data.getString("status").equals("COMPLETED")) {
                        chunkTasksCompleted++;
                    }
                }
                //If all chunk tasks completed then stop waiting and proceed to mapping
                if(chunkTasksCompleted==j.getTasks().size()){
                    break;
                }
                Thread.sleep(500);
            }
            endTime = System.currentTimeMillis();

            //Calculate Chunking Time elapsed
            diffTime = (endTime-startTime)/1000.0;
            tasksForStage = (int) numOfWorkers;

            //Update chunking stage time plus num of workers in ZK for current job
            zController.updateStatsOfStage(j.getJobZnode(), "chunking", diffTime, tasksForStage);
            j.deqeueAll();



            System.out.println("\nProceeding to Mapping...");
            startTime = System.currentTimeMillis();
            //Find out what chunks where created at the chunk stage
            String jobResultDir = "/app/uploads/results/"+j.getJobZnode();
            List<String> chunkFiles = scanDirectory(jobResultDir,"chunked_");

            int index = 0;

            zController.updateJobStatus(j.getJobZnode(), "mapping");
            for(String s: chunkFiles){
                s = jobResultDir + "/"+s;
                //Proceed to form MapTasks for each of the chunks
                String id = "t"+UUID.randomUUID().toString().replace("-","");
                String tZnodePath = j.getJobZnode() + "/tasks/" + id;
                MapTask t = new MapTask(id, "/jobs/"+tZnodePath, "map", TaskType.MAP, index, "java -cp /app/uploads/"+user+"/executables/"+j.getExecutableJar()+" Control "+j.getMapCommand()+" "+s+" "+jobResultDir+"/map_"+index+".intermediate");
                index++;
                System.out.println("Created Map Task with exec cmd: "+t.getFunctionName());
                zController.insertTaskToZK(t);

                while (true) {
                    String worker = reserveWorker();
                    if(worker==null || worker.equals("null")){
                        continue;
                    }
                    System.out.println("Trying to assign task: "+ t.getZnodePath()+" to reserved worker: "+worker);
                    if(assignTask(t,worker)){
                        t.setOnWorker(worker);
                        System.out.println("Will insert assignment for task: " + id + " at job:"+j.getJobZnode()+" on worker: "+worker);
                        zController.insertAssignmentToZK(j.getJobZnode(), id, worker);
                        j.enqeueTask(t);
                        break;
                    }
                    Thread.sleep(10);
                    System.out.println("Failed to assign, trying again...");
                }
            }

            while(true){
                int chunkTasksCompleted = 0;
                System.out.println(zController.getTasksOfJob("/jobs/"+j.getJobZnode()));
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    JsonObject data = zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s);
                    if(data.getString("command").equals("map") && data.getString("status").equals("COMPLETED")) {
                        chunkTasksCompleted++;
                    }
                }
                //If all chunk tasks completed then stop waiting and proceed to mapping
                if(chunkTasksCompleted==j.getTasks().size()){
                    break;
                }
                Thread.sleep(5000);
            }

            deleteFilesWithPrefix(jobResultDir,"chunked_");
            j.deqeueAll();


            endTime = System.currentTimeMillis();
            //Calculate Mapping Time elapsed
            diffTime = (endTime-startTime)/1000.0;
            tasksForStage = (int) chunkFiles.size();
            //Update mapping stage time plus num of workers in ZK for current job
            zController.updateStatsOfStage(j.getJobZnode(), "mapping", diffTime, tasksForStage);



            System.out.println("\nProceeding to Shuffling...");
            startTime = System.currentTimeMillis();


            List<String> mapfiles = scanDirectory(jobResultDir,"map_");
            // Convert the string list to a JSON array
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (String str : mapfiles) {
                jsonArrayBuilder.add(jobResultDir+"/"+str);
            }
            JsonArray jsonArray = jsonArrayBuilder.build();
            // Create the larger JSON object with the key "files"
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("files", jsonArray)
                    .build();

            String id = "t"+UUID.randomUUID().toString().replace("-","");
            String tZnodePath = j.getJobZnode() + "/tasks/" + id;
            ShuffleTask st = new ShuffleTask(id,"shuffle","/jobs/"+tZnodePath,  TaskType.SHUFFLE, jobResultDir, j.getNumberOfReducers(), Jsonizer.jsonObjectToString(jsonObject));
            zController.insertTaskToZK(st);

            while (true) {
                String worker = reserveWorker();
                if(worker==null || worker.equals("null")){
                    continue;
                }
                System.out.println("Trying to assign task: "+ st.getZnodePath()+" to reserved worker: "+worker);
                if(assignTask(st,worker)){
                    st.setOnWorker(worker);
                    System.out.println("Will insert assignment for task: " + id + " at job:"+j.getJobZnode()+" on worker: "+worker);
                    zController.insertAssignmentToZK(j.getJobZnode(), id, worker);
                    j.enqeueTask(st);
                    break;
                }
                Thread.sleep(10);
                System.out.println("Failed to assign, trying again...");
            }

            zController.updateJobStatus(j.getJobZnode(), "shuffling");

            while(true){
                int shuffleTasksCompleted = 0;
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    JsonObject data = zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s);
                    if(data.getString("command").equals("shuffle") && data.getString("status").equals("COMPLETED")) {
                        shuffleTasksCompleted++;
                    }
                }
                //If the only shuffle task is completed then stop waiting and proceed to reducing
                if(shuffleTasksCompleted==1){
                    break;
                }
                Thread.sleep(5000);
            }

            deleteFilesWithPrefix(jobResultDir,"map_");
            j.deqeueAll();
            endTime = System.currentTimeMillis();
            //Calculate Shuffling Time elapsed
            diffTime = (endTime-startTime)/1000.0;
            tasksForStage = 1;
            //Update shuffling stage time plus num of workers in ZK for current job
            zController.updateStatsOfStage(j.getJobZnode(), "shuffling", diffTime, tasksForStage);





            System.out.println("\nProceeding to Reducing...");
            startTime = System.currentTimeMillis();

            //Find out what intermediate files where created at the shuffle stage
            List<String> reduceFiles = scanDirectory(jobResultDir,"reduce_");
            //Reset index for distinct file name for the merge intermediate files
            index = 0;
            for(String s: reduceFiles){
                s = "/app/uploads/results/" + j.getJobZnode() + "/"+s;
                //Proceed to form MapTasks for each of the chunks
                id = "t"+UUID.randomUUID().toString().replace("-","");
                tZnodePath = j.getJobZnode() + "/tasks/" + id;
                ReduceTask t = new ReduceTask(id, "reduce","/jobs/"+tZnodePath, TaskType.REDUCE, s, jobResultDir, index,"java -cp /app/uploads/"+user+"/executables/"+j.getExecutableJar()+" Control "+j.getReduceCommand()+" "+s+" "+jobResultDir+"/ "+index);
                index++;

                System.out.println("Created Reduce Task for intermediate input file: "+s);
                zController.insertTaskToZK(t);

                while (true) {
                    String worker = reserveWorker();
                    if(worker==null || worker.equals("null")){
                        continue;
                    }
                    System.out.println("Trying to assign task: "+ t.getZnodePath()+" to reserved worker: "+worker);
                    if(assignTask(t,worker)){
                        t.setOnWorker(worker);
                        System.out.println("Will insert assignment for task: " + id + " at job:"+j.getJobZnode()+" on worker: "+worker);
                        zController.insertAssignmentToZK(j.getJobZnode(), id, worker);
                        j.enqeueTask(t);
                        break;
                    }
                    Thread.sleep(10);
                    System.out.println("Failed to assign, trying again...");
                }
            }
            zController.updateJobStatus(j.getJobZnode(), "reducing");

            while(true){
                int reduceTasksCompleted = 0;
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    JsonObject data = zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s);
                    if(data.getString("command").equals("reduce") && data.getString("status").equals("COMPLETED")) {
                        reduceTasksCompleted++;
                    }
                }
                //If all reduce tasks are completed then we proceed to the next stage
                if(reduceTasksCompleted==j.getNumberOfReducers()){
                    break;
                }
                Thread.sleep(5000);
            }

            deleteFilesWithPrefix(jobResultDir,"reduce_");
            j.deqeueAll();
            endTime = System.currentTimeMillis();
            //Calculate Reducing Time elapsed
            diffTime = (endTime-startTime)/1000.0;
            tasksForStage = j.getNumberOfReducers();
            //Update reducing stage time plus num of workers in ZK for current job
            zController.updateStatsOfStage(j.getJobZnode(), "reducing", diffTime, tasksForStage);


            System.out.println("\nComputational stages of job: "+j.getJobZnode()+" completed, proceeding to merging...");
            startTime = System.currentTimeMillis();


            List<String> mergefiles = scanDirectory(jobResultDir,"result_");
            // Convert the string list to a JSON array
            for (String str : mergefiles) {
                jsonArrayBuilder.add(jobResultDir+"/"+str);
            }
            JsonArray mergeFilesArray = jsonArrayBuilder.build();
            // Create the larger JSON object with the key "files"
            JsonObject jsonMergeFiles = Json.createObjectBuilder()
                    .add("files", mergeFilesArray)
                    .build();

            id = "t"+UUID.randomUUID().toString().replace("-","");
            tZnodePath = j.getJobZnode() + "/tasks/" + id;
            MergeTask mt = new MergeTask(id,"merge","/jobs/"+tZnodePath,  TaskType.MERGE, jobResultDir+"/", Jsonizer.jsonObjectToString(jsonMergeFiles));
            zController.insertTaskToZK(mt);

            while (true) {
                String worker = reserveWorker();
                if(worker==null || worker.equals("null")){
                    continue;
                }
                System.out.println("Trying to assign task: "+ mt.getZnodePath()+" to reserved worker: "+worker);
                if(assignTask(mt,worker)){
                    mt.setOnWorker(worker);
                    System.out.println("Will insert assignment for task: " + id + " at job:"+j.getJobZnode()+" on worker: "+worker);
                    zController.insertAssignmentToZK(j.getJobZnode(), id, worker);
                    j.enqeueTask(mt);
                    break;
                }
                Thread.sleep(10);
                System.out.println("Failed to assign, trying again...");
            }

            zController.updateJobStatus(j.getJobZnode(), "merging");

            while(true){
                int mergeTasksCompleted = 0;
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    JsonObject data = zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s);
                    if(data.getString("command").equals("merge") && data.getString("status").equals("COMPLETED")) {
                        mergeTasksCompleted++;
                    }
                }
                //If all reduce tasks are completed then we proceed to the next stage
                if(mergeTasksCompleted==1){
                    break;
                }
                Thread.sleep(5000);
            }

            zController.updateJobStatus(j.getJobZnode(), "completed");
            deleteFilesWithPrefix(jobResultDir,"result_");

            endTime = System.currentTimeMillis();
            //Calculate Mering Time elapsed
            diffTime = (endTime-startTime)/1000.0;
            tasksForStage = 1;
            //Update Merging stage time plus num of workers in ZK for current job
            zController.updateStatsOfStage(j.getJobZnode(), "merging", diffTime, tasksForStage);

            System.out.println("Job Completed");
        }
        else {
            System.out.println("Input file does not exist. Job Failed. Exiting...");
            System.out.println("Job cannot be executed, marking it as failed...");
            zController.updateJobStatus(j.getJobZnode(), "failed");
            System.exit(1);
        }
    }



    public boolean handleWorkerFailure(String workerFailed){
        System.out.println("Will handle worker + "+workerFailed + " crash in case any tasks were running on him");
        return true;
    }


    public static List<String> scanDirectory(String directoryPath, String prefix) {
        List<String> fileList = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path) && path.getFileName().toString().startsWith(prefix)) {
                    fileList.add(path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileList;
    }

    public boolean assignTask(Task t, String worker) throws Exception {
        String url = "http://"+zController.getWorkerData(worker).getString("ipAddress")+":6000/api/task/assign?monitor="+this.handlerMonitor+"&tid="+t.getZnodePath();
        int status = HttpClient.post(url, null);
        System.out.println("Worker said : "+status);
        if(status==200){
            return true;
        }
        return false;
    }

    public String reserveWorker() throws Exception{
        List<String> idleWorkers;
        try {
            idleWorkers = zController.getAllIdleWorkers();
        } catch(Exception e){
            idleWorkers = new ArrayList<String>();
            System.out.println("Totally not idle workers in the system, will deploy...");
        }

        //Select one of the idle workers
        if(!idleWorkers.isEmpty()){
            //Choose randomly an idle worker in order to minimize the probability of conflict
            String currW = idleWorkers.get(random.nextInt(0,idleWorkers.size()));
            System.out.println("Found idling worker: "+currW+", will assign job to him");
            String ipAddress = zController.getWorkerData(currW).getString("ipAddress");
            String url = "http://"+ipAddress+":6000/api/reserve/"+this.handlerMonitor;
            if(HttpClient.post(url, null)==200){
                if(zController.getWorkerData(currW).getString("status").equals("reserved")){
                    return currW;
                }else{
                    return null;
                }
            }else{
                return null;
            }
        }else{
            //If no idle workers are available then deploy a new one
            String id = "w"+UUID.randomUUID().toString().replace("-","");
            dController.deployWorker(id,this.handlerMonitor);
            System.out.println("Deployment successful");
            try {
                // Sleep for 2 seconds to allow worker to initialize
                Thread.sleep(3300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(zController.getWorkerData(id).getString("status").equals("reserved")){
                return id;
            }else{
                return null;
            }
        }
    }


    private void deleteFilesWithPrefix(String directory, String prefix) {
        File dir = new File(directory);
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix));

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    boolean isDeleted = file.delete();
                }
            }
        }
    }
}


