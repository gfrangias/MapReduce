package org.example;

import model.ChunkTask;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class TaskController {
    private String taskZnodePath;
    private ZNodeController zController;

    public TaskController(ZNodeController zc){
        this.zController = zc;
    }

    public void handleTask(String tid) throws Exception {
        this.taskZnodePath = tid;
        System.out.println("Will handle task with znode path: "+tid);
        JsonObject tData =  zController.getTaskData(taskZnodePath);
        //tData.getJsonNumber().longValue()
        System.out.println("Task data: "+Jsonizer.jsonObjectToString(tData));
        String taskType = tData.getString("command");
        //Decide whats the type of the type of task to be handled and
        //decide flow of execution appropriately
        zController.updateTaskStatus(tid, TaskStatus.RUNNING);
        long noc = tData.getJsonNumber("numOfChunks").longValue();
        long offset = tData.getJsonNumber("offset").longValue();
        long csz = tData.getJsonNumber("chunkSize").longValue();
        ChunkTask t = new ChunkTask(tid, tData.getString("onworker"), tData.getString("command"), tid, TaskType.CHUNK, "/app/"+tData.getString("dataset"), noc, offset, csz);
        //CHUNK TASK
        if(taskType.equals("chunk")){
            System.out.println("Following execution pattern for Chunk Task");
            String znodePath = t.getZnodePath();

            String[] parts = znodePath.split("/");

            StringBuilder buffer = new StringBuilder();
            for (int i=2; i<4; i++) {
                buffer.append("/" + parts[i]);
            }
            System.out.println("Will store chunks in "+buffer.toString());
            String outputPath = "/app/uploads/results" + buffer.toString();
            File dir = new File(outputPath);
            if (!dir.exists()) {
                // Attempt to create the directory
                boolean created = dir.mkdirs();
                if (created) {
                    System.out.println("Directory was created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            } else {
                System.out.println("Directory already exists!");
            }
            System.out.println(buffer.toString());
            outputPath = outputPath+"/";
            Distributor distributor = new Distributor(t.getDatasetPath(), outputPath, t.getChunkSize(), t.getOffSetAtFile(), t.getNumOfChunks());
            boolean taskResult = distributor.create_chunk_file();
            if(taskResult){
                zController.updateTaskStatus(tid, TaskStatus.COMPLETED);
            }else{
                zController.updateTaskStatus(tid, TaskStatus.FAILED);
            }
        }else{
            System.out.println("Task invalid. Failure");
            zController.updateTaskStatus(tid, TaskStatus.FAILED);
        }
    }

    public String systemExecTask(String cmd){
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result = line+"\n";
            }
            reader.close();
        } catch (IOException e) {
            return e.getMessage();
        }
        return result;
    }
    public void goIntoIdle(){

    }
    public String writeResultsOfTaskToZk(){
        return null;
    }

}
