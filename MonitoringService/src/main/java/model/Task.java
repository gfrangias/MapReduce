package model;

public class Task {

    private String command;
    private String onWorker;
    private TaskStatus status;
    private TaskType type;
    private String inputPath;
    private String functionalPath;
    private String outputPath;

    public Task(String cmd, String onw, TaskType type, String input){
        this.command = cmd;
        this.onWorker = onw;
        this.type = type;
        this.inputPath = input;
    }

}
