package model;

public class MergeTask extends Task{
    private String outputPath;
    private String[] jsonFileArray;

    public MergeTask(String id, String worker, String cmd, String znodePath, TaskType type, String outP, String[] jsonFileArray) {
        super(id, worker, cmd, znodePath, type);
        this.outputPath = outP;
        this.jsonFileArray = jsonFileArray;
    }
    public String getOutputPath() {
        return outputPath;
    }
    public String[] getJsonFileArray() {
        return jsonFileArray;
    }
}
