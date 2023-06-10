package model;

import java.util.Vector;

public class Job {
    private String mapCommand;
    private String reduceCommand;
    private String chunkCommand;
    private String shuffleCommand;
    private String mergeCommand;
    private String executableJar;
    private String jobZnode;
    private String globalInputPath;
    private Vector<Task> tasks;

    public Job(String input, String jar, String znode){
        this.tasks = new Vector<Task>();
        this.executableJar = jar;
        this.globalInputPath = input;
        this.jobZnode = znode;
    }

    public void setJobCommands(String map, String reduce, String chunk, String shuffle, String merge){
        this.mapCommand = map;
        this.reduceCommand = reduce;
        this.chunkCommand = chunk;
        this.shuffleCommand = shuffle;
        this.mergeCommand = merge;
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

    public String getReduceCommand() {
        return reduceCommand;
    }

    public String getChunkCommand() {
        return chunkCommand;
    }

    public String getShuffleCommand() {
        return shuffleCommand;
    }

    public String getMergeCommand() {
        return mergeCommand;
    }

    public void enqeueTask(Task t){
        tasks.add(t);
    }
    public Task deqeueTask() {
        Task t = tasks.lastElement();
        tasks.remove(tasks.indexOf(t));
        return t;
    }
}
