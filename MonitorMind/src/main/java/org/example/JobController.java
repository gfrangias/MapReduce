package org.example;

import model.*;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


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
    private static final long CHUNK_SIZE = 1 << 26L;
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

        Job j = new Job(datasetPath, jobInfoObj.getString("executable_file"),forUser+"/"+jobNodeId);
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
        
        if (file.exists()) {
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
                        zController.updateWorkerOfTask(t.getZnodePath(), worker);
                        j.enqeueTask(t);
                        break;
                    }
                    Thread.sleep(10);
                    System.out.println("Failed to assign, trying again...");
                }
            }

            System.out.println(j.getTasks());
            zController.updateJobStatus(j.getJobZnode(), "chunking");
            while(true){
                int chunkTasksCompleted = 0;
                System.out.println(zController.getTasksOfJob("/jobs/"+j.getJobZnode()));
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    System.out.println(zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s));
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

            System.out.println("Proceeding to Mapping...");

            //Find out what chunks where created at the chunk stage
            String jobResultDir = "/app/uploads/results/"+j.getJobZnode();
            List<String> chunkFiles = scanDirectory(jobResultDir);

            int index = 0;
            zController.updateJobStatus(j.getJobZnode(), "mapping");


            for(String s: chunkFiles){
                s = "/app/uploads/results/" + j.getJobZnode() + "/"+s;
                //Proceed to form MapTasks for each of the chunks
                String id = "t"+UUID.randomUUID().toString().replace("-","");
                String tZnodePath = j.getJobZnode() + "/tasks/" + id;
                MapTask t = new MapTask(id, "/jobs/"+tZnodePath, "map", TaskType.MAP, index, "java -cp /app/uploads/"+user+"/executables/"+j.getExecutableJar()+" "+j.getMapCommand()+" "+s+" > "+jobResultDir+"/map_"+index+".intermediate");
                index++;
                System.out.println("Created task with exec cmd: "+t.getFunctionName());
                zController.insertTaskToZK(t);
                System.out.println(t.getZnodePath());


                while (true) {
                    String worker = reserveWorker();
                    if(worker==null || worker.equals("null")){
                        continue;
                    }
                    System.out.println("Trying to assign task: "+ t.getZnodePath()+" to reserved worker: "+worker);
                    if(assignTask(t,worker)){
                        t.setOnWorker(worker);
                        zController.updateWorkerOfTask(t.getZnodePath(), worker);
                        j.enqeueTask(t);
                        break;
                    }
                    Thread.sleep(10);
                    System.out.println("Failed to assign, trying again...");
                }
            }

            System.out.println(j.getTasks());
            zController.updateJobStatus(j.getJobZnode(), "chunking");
            while(true){
                int chunkTasksCompleted = 0;
                System.out.println(zController.getTasksOfJob("/jobs/"+j.getJobZnode()));
                for(String s : zController.getTasksOfJob("/jobs/"+j.getJobZnode())){
                    System.out.println(zController.getZnodeData("/jobs/"+j.getJobZnode() + "/tasks/" + s));
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
        }
        else {
            System.out.println("Input file does not exist. Job Failed. Exiting...");
            System.out.println("Job cannot be executed, marking it as failed...");
            zController.updateJobStatus(j.getJobZnode(), "failed");
            System.exit(1);
        }
    }

    public static List<String> scanDirectory(String directoryPath) {
        List<String> fileList = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path) && path.getFileName().toString().startsWith("chunked_")) {
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
}


