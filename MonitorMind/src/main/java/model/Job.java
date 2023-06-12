package model;

import java.util.LinkedList;

public class Job {
    private String mapCommand;
    private String reduceCommand;
    private String executableJar;
    private String jobZnode;
    private String globalInputPath;
    private LinkedList<Task> tasks;

    public Job(String input, String jar, String znode){
        this.tasks = new LinkedList<Task>();
        this.executableJar = jar;
        this.globalInputPath = input;
        this.jobZnode = znode;
    }

    public void setJobCommands(String map, String reduce){
        this.mapCommand = map;
        this.reduceCommand = reduce;
    }

    public String getGlobalInputPath() {
        return globalInputPath;
    }

    public String getJobZnode() {
        return jobZnode;
    }

    public String getExecutableJar() {
        return executableJar;
    }

    public String getMapCommand() {
        return mapCommand;
    }


    public void enqeueTask(Task t){
        tasks.push(t);
    }
    public Task deqeueTask() {
        return tasks.removeLast();
    }

    public void deqeueAll() {tasks = new LinkedList<Task>();}

    public LinkedList<Task> getTasks(){
        return tasks;
    }
}
