package model;

import java.util.Vector;

public class Job {
    private String mapClass;
    private String reduceClass;

    private String jobZnode;

    private String globalInputPath;
    private Vector<Task> tasks;

    public Job(String input, String znode){
        this.tasks = new Vector<Task>();
        this.globalInputPath = input;
        this.jobZnode = znode;
    }

    public String getGlobalInputPath() {
        return globalInputPath;
    }

    public String getJobZnode() {
        return jobZnode;
    }
}
