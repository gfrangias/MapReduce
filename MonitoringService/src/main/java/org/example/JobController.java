package org.example;

import javax.json.JsonObject;

public class JobController {

    private DockerController dController;
    private ZNodeController zController;

    public JobController(DockerController dc, ZNodeController zc){
        this.dController = dc;
        this.zController = zc;
    }

    /**
     * This method is called via the web server API endpoint /api/job/assign/$id.
     * It starts the process of job handling. First it calculates the number of workers
     * needed for the job. Based on the type of the job the appropriate steps are calculated
     * @param jobNodeId
     */
    public void handleJob(String jobNodeId) throws Exception{
        JsonObject jobInfoObj = zController.getJobData(jobNodeId);
        String executable = jobInfoObj.getString("executable");
        String dataset = jobInfoObj.getString("dataset");
        String type = jobInfoObj.getString("jobtype");

        //Should decide the number of workers based on the size of file
        //and deploy them
        int numOfWorkers = 10;

    }

    /**
     *
     */
    public void reassignJob(String jobNodeId){

    }


}


