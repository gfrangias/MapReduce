package model;

public class ShuffleTask extends Task{

    private String outputPath;
    private int numOfReducers;
    private String[] jsonFileArray;
    public ShuffleTask(String id, String worker, String cmd, String znodePath, TaskType type, String out, int nor, String[] jsonFileArray) {
        super(id, worker, cmd, znodePath, type);
        this.outputPath = out;
        this.numOfReducers = nor;
        this.jsonFileArray = jsonFileArray;
    }
    public String getOutputPath() {
        return outputPath;
    }

    public int getNumOfReducers() {
        return numOfReducers;
    }

    public String[] getJsonFileArray() {
        return jsonFileArray;
    }
}
