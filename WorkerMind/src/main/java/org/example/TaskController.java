package org.example;

import model.*;

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
        //CHUNK TASK
        if(taskType.equals("chunk")){
            long noc = tData.getJsonNumber("numOfChunks").longValue();
            long offset = tData.getJsonNumber("offset").longValue();
            long csz = tData.getJsonNumber("chunkSize").longValue();
            ChunkTask t = new ChunkTask(tid, tData.getString("onworker"), tData.getString("command"), tid, TaskType.CHUNK, "/app/"+tData.getString("dataset"), noc, offset, csz);

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


        }else if(taskType.equals("map")){
            System.out.println("Accepted Map Task");
            int index = tData.getJsonNumber("index").intValue();
            MapTask t = new MapTask(tid,tData.getString("onworker"), tid, tData.getString("command"), TaskType.MAP, index, tData.getString("function"));
            System.out.println(t.toString());
            String result = systemExecTask(tData.getString("function"));
            if(result.equals("error")){
                zController.updateTaskStatus(tid, TaskStatus.COMPLETED);
                System.out.println("Success");
            }else{
                zController.updateTaskStatus(tid, TaskStatus.FAILED);
                System.out.println("Failed");
            }
        }  else{
            System.out.println("Task invalid. Failure");
            zController.updateTaskStatus(tid, TaskStatus.FAILED);
        }
    }

    public String systemExecTask(String command){
        try {
            // Execute the command
            System.out.println("Will execute command: "+ command);
            Process process = new ProcessBuilder(command).start();
            // Wait for the process to finish
            int exitCode = process.waitFor();

            // Optionally, handle the exit code or perform other actions based on the result
            if (exitCode == 0) {
                System.out.println("JAR execution completed successfully.");
                return String.valueOf(exitCode);
            } else {
                System.out.println("JAR execution failed with exit code: " + exitCode);
                return "error";
            }
        } catch (IOException | InterruptedException e) {
            return "error";
        }
    }
}
