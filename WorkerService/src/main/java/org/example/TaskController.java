package org.example;

import model.TaskStatus;

import javax.json.JsonObject;
import java.io.BufferedReader;
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
        System.out.println("Task data: "+Jsonizer.jsonObjectToString(tData));
        String taskType = tData.getString("command");
        //Decide whats the type of the type of task to be handled and
        //decide flow of execution appropriately

        //CHUNK TASK
        if(taskType.equals("chunk")){
            System.out.println("Following execution pattern for Chunk Task");
            String taskResult = systemExecTask();
            if(taskResult!=null){
                System.out.println("Requested task resulted in :"+taskResult);
                //.updateTaskStatus(tid, TaskStatus.COMPLETED);
            }else{
                System.out.println("Task failed: "+taskResult);
                //zController.updateTaskStatus(tid, TaskStatus.FAILED);
            }
        }else{
            System.out.println("Task invalid. Failure");
            zController.updateTaskStatus(tid, TaskStatus.FAILED);
        }
    }

    public String systemExecTask(){
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec("echo Hello World");
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
