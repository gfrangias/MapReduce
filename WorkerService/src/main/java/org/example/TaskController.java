package org.example;

import javax.json.JsonObject;

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
    }

    public String systemExecTask(){
        return null;
    }

    public String writeResultsOfTaskToZk(){
        return null;
    }

}
