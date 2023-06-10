package model;

public class Task {

    protected String taskId;
    protected String znodePath;
    protected String command;
    protected String onWorker;
    protected TaskStatus status;

    protected TaskType taskType;

    public Task(String id, String worker, String cmd, String znodePath, TaskType type){
        this.taskId = id;
        this.onWorker = worker;
        this.znodePath = znodePath;
        this.command = cmd;
        this.status = TaskStatus.QUEUED;
        this.taskType = type;
    }

    public void setTaskRunning(){ this.status = TaskStatus.RUNNING; }

    public void setTaskCompleted(){
        this.status = TaskStatus.COMPLETED;
    }

    public void setTaskFailed(){
        this.status = TaskStatus.FAILED;
    }

}
