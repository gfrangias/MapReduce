package org.example;

import model.ChunkTask;
import model.Job;
import model.Task;
import java.util.UUID;


import javax.json.JsonObject;
import javax.swing.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Vector;

public class JobController {

    private ZNodeController zController;
    private static long CHUNK_SIZE = 67108864L;
    private static int MAX_WORKERS_PER_JOB = 30;
    private Job job;


    public JobController( ZNodeController zc){

        this.zController = zc;
    }

    /**
     * This method is called via the web server API endpoint /api/job/assign/$id.
     * It starts the process of job handling. First it calculates the number of workers
     * needed for the job. Based on the type of the job the appropriate steps are calculated
     * @param jobNodeId
     * @param forUser
     */
    public void handleJob(String jobNodeId, String forUser) throws Exception{
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

        Vector<Long> offsets_for_i = new Vector<Long>();
        Vector<Integer> chunks_for_i = new Vector<Integer>();

        long fileSize = 0;
        int numOfWorkers = 0;
        int nMinChunksPerWorker = 10;
        int numOfChunksPerWorker = 0;

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

            if(MAX_WORKERS_PER_JOB*CHUNK_SIZE*nMinChunksPerWorker <= fileSize){
                numOfWorkers = MAX_WORKERS_PER_JOB;
                numOfChunksPerWorker = (int) Math.ceil((double)1.0* fileSize/(MAX_WORKERS_PER_JOB*CHUNK_SIZE));
            }else {
                numOfChunksPerWorker = nMinChunksPerWorker;
                numOfWorkers = (int) Math.ceil((double) 1.0 * fileSize / (nMinChunksPerWorker * CHUNK_SIZE));
            }


            for (int i=0; i<numOfWorkers; i++) {

                long offsetI = i * (fileSize / numOfWorkers) / CHUNK_SIZE;
                offsets_for_i.add(offsetI);
                if (i == numOfWorkers - 1) {
                    BigDecimal fsz = new BigDecimal(fileSize);
                    BigDecimal csz = new BigDecimal(CHUNK_SIZE);
                    BigDecimal res = fsz.divide(csz, 10, RoundingMode.CEILING);
                    int totalChunks = res.setScale(0, RoundingMode.CEILING).intValue();
                    int othersHave = (numOfWorkers-1)*numOfChunksPerWorker;
                    chunks_for_i.add(totalChunks-othersHave);
                } else {
                    chunks_for_i.add(numOfChunksPerWorker);
                }
            }
            System.out.println(" > Will deploy "+numOfWorkers+" workers to be used for chunking of the dataset.");
            System.out.println(" > Each worker will create "+numOfChunksPerWorker+" chunks of 64MB totaling: "+numOfChunksPerWorker*(numOfWorkers-1)+ " chunks.");
            System.out.println(" > Last worker will create "+chunks_for_i.lastElement()+" chunks of 64MB.");

            //Enqueue Chunk tasks
            for ( int w=0; w<nMinChunksPerWorker; w++){
                String id = "t"+UUID.randomUUID().toString().replace("-","");
                Task t = new ChunkTask(id, j.getGlobalInputPath(), numOfChunksPerWorker, offsets_for_i.get(w), CHUNK_SIZE);
                j.enqeueTask(t);

            }

            //Deploy workers and assign chunk tasks


        } else {
            System.out.println("Input file does not exist. Job Failed. Exiting...");
            System.out.println("Job cannot be executed, marking it as failed...");
            zController.updateJobStatus(j.getJobZnode(), "failed");
            System.exit(0);
        }
    }
}


