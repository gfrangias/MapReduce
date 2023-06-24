package org.example;

import model.*;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class TaskController {
    private String taskZnodePath;
    private ZNodeController zController;

    public TaskController(ZNodeController zc){
        this.zController = zc;
    }

    public void handleTask(String tid, String workerName) throws Exception {
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
            zController.makeMeIdle(workerName);


        } else if(taskType.equals("map")){

            System.out.println("Accepted Map Task");
            int index = tData.getJsonNumber("index").intValue();
            MapTask t = new MapTask(tid,tData.getString("onworker"), tid, tData.getString("command"), TaskType.MAP, index, tData.getString("function"));
            System.out.println(t.toString());

            spawnProcess(tData.getString("function"), tid, workerName);


        } else if(taskType.equals("shuffle")){

            System.out.println("Accepted Shuffle Task");
            int numOfReducers = tData.getJsonNumber("numOfReducers").intValue();
            String outputPath = tData.getString("outputPath");
            // Get the "files" field as a JSON array
            JsonArray filesArray = tData.getJsonArray("files");

            // Convert the JSON array to a string array
            String[] files = new String[filesArray.size()];
            for (int i = 0; i < filesArray.size(); i++) {
                files[i] = filesArray.getString(i);
            }

            for (String file : files) {
                System.out.println(file);
            }

            ShuffleTask t = new ShuffleTask(tid, tData.getString("onworker"), tData.getString("command"), tid, TaskType.SHUFFLE, tData.getString("outputPath"), tData.getInt("numOfReducers"), files);
            try{
                Shuffler.shuffle(t.getJsonFileArray(),t.getNumOfReducers(),t.getOutputPath());
                zController.updateTaskStatus(tid, TaskStatus.COMPLETED);
            }catch(Exception e){
                zController.updateTaskStatus(tid, TaskStatus.FAILED);
            }
            zController.makeMeIdle(workerName);

        } else if(taskType.equals("reduce")){


            System.out.println("Accepted Reduce Task");

            int index = Integer.parseInt(tData.getString("index"));
            String input = tData.getString("inputPath");
            String output = tData.getString("outputPath");
            String fcommand =  tData.getString("fcommand");
            ReduceTask t = new ReduceTask(tid,tData.getString("onworker"), tid, tData.getString("command"), TaskType.REDUCE, input, output, index, fcommand);
            System.out.println(t.toString());

            spawnProcess(t.getFunctionCommand(), tid, workerName);



        } else if(taskType.equals("merge")){


            System.out.println("Accepted Merge Task");
            String outputPath = tData.getString("outputPath");
            // Get the "files" field as a JSON array
            JsonArray filesArray = tData.getJsonArray("files");

            // Convert the JSON array to a string array
            String[] files = new String[filesArray.size()];
            for (int i = 0; i < filesArray.size(); i++) {
                files[i] = filesArray.getString(i);
            }

            for (String file : files) {
                System.out.println(file);
            }

            MergeTask t = new MergeTask(tid, tData.getString("onworker"), tData.getString("command"), tid, TaskType.MERGE, tData.getString("outputPath"), files);
            try{
                Merger.merge(t.getOutputPath(), t.getJsonFileArray());
                zController.updateTaskStatus(tid, TaskStatus.COMPLETED);
            }catch(Exception e){
                zController.updateTaskStatus(tid, TaskStatus.FAILED);
            }
            zController.makeMeIdle(workerName);


        } else{
            System.out.println("Task invalid. Failure");
            zController.updateTaskStatus(tid, TaskStatus.FAILED);
        }
    }

    public void spawnProcess(String cmd, String tid, String workerName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process process = processBuilder.start();

        // Create a separate thread to wait for the process to complete
        Thread waitThread = new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    zController.updateTaskStatus(tid, TaskStatus.COMPLETED);

                } else {
                    zController.updateTaskStatus(tid, TaskStatus.FAILED);
                }
                zController.makeMeIdle(workerName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Start the wait thread
        waitThread.start();
    }
}
