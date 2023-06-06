package org.example;

import model.Job;
import model.Task;

import javax.json.JsonObject;
import java.io.File;
import java.util.Vector;

public class JobController {

    private DockerController dController;
    private ZNodeController zController;
    private static long CHUNK_SIZE = 1<<26L;
    private Job job;

    public JobController(DockerController dc, ZNodeController zc){
        this.dController = dc;
        this.zController = zc;
    }

    public static void main(String[] args) {
        System.out.println(CHUNK_SIZE);
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
        Job j = new Job(datasetPath, forUser+"/"+jobNodeId);
        zController.updateJobStatus(j.getJobZnode(), "planning");

        //Parse File to check for input size
        System.out.println(" > Will check input file size to decide chunking...");
        File file = new File(j.getGlobalInputPath());
        System.out.println(file.getAbsolutePath());

        long fileSize = 0;
        int numOfWorkers = 0;

        if (file.exists()) {
            fileSize = file.length();
            System.out.println(" > File size in bytes: " + fileSize);
            System.out.println(" > Calculating workers needed for chunk size: 64 MB.");
            numOfWorkers = (int) Math.ceil((double)1.0*fileSize/CHUNK_SIZE);
            System.out.println(" > Will deploy "+numOfWorkers+" workers to be used for chunking of the dataset.");
            zController.updateJobStatus(j.getJobZnode(), "resourcing");

            //dController.deploySwarmOfWorkers(2, father:this);
        } else {
            System.out.println("Input file does not exist. Job Failed. Exiting...");
            System.out.println("Job cannot be executed, marking it as failed...");
            zController.updateJobStatus(j.getJobZnode(), "failed");
            System.exit(0);
        }
    }

}


