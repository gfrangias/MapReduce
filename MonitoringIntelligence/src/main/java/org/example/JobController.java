package org.example;

import model.ChunkTask;
import model.Job;
import model.Task;
import model.TaskType;

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
                Task t = new ChunkTask(id,"chunk", tZnodePath, TaskType.CHUNK, j.getGlobalInputPath(), chunks_for_i[w], offsets_for_i[w], CHUNK_SIZE);
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
                }
            }
            System.out.println(j.getTasks());
            zController.updateJobStatus(j.getJobZnode(), "chunking");

            //Deploy workers and assign chunk tasks
        } 
        else {
            System.out.println("Input file does not exist. Job Failed. Exiting...");
            System.out.println("Job cannot be executed, marking it as failed...");
            zController.updateJobStatus(j.getJobZnode(), "failed");
            System.exit(1);
        }
    }

    public boolean assignTask(Task t, String worker) throws Exception {
        String url = "http://"+zController.getWorkerData("worker").getString("ipAddress")+"/api/task/assign?monitor"+this.handlerMonitor+"&tid="+t.getZnodePath();
        if(HttpClient.post(url, null)==200){
            return true;
        }
        return false;
    }

    public String reserveWorker() throws Exception{
        List<String> idleWorkers = zController.getAllIdleWorkers();
        //Select one of the idle workers
        if(!idleWorkers.isEmpty()){
            //Choose randomly an idle worker in order to minimize the probability of conflict
            String currW = idleWorkers.get(random.nextInt(0,idleWorkers.size()));
            String ipAddress = zController.getWorkerData(currW).getString("ipAddress");
            String url = "http://"+ipAddress+":6000/api/reserve/"+this.handlerMonitor;
            if(HttpClient.post(url, null)==200 && zController.getWorkerData(currW).getString("status")=="reserved"){
                return currW;
            }else{
                return null;
            }
        }else{
            //If no idle workers are available then deploy a new one
            String id = "w"+UUID.randomUUID().toString().replace("-","");
            dController.deployMonitor(id,this.handlerMonitor);
            try {
                // Sleep for 2 seconds to allow worker to initialize
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(zController.getWorkerData(id).getString("status")=="reserved"){
                return id;
            }else{
                return null;
            }
        }
    }
}


