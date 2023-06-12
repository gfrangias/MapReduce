package model;

public class ShuffleTask extends Task{

    private String outputPath;
    private int numOfReducers;
    private String jsonFileArray;
    public ShuffleTask(String id, String cmd, String znodePath, TaskType type, String out, int nor, String jsonFileArray) {
        super(id, cmd, znodePath, type);
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

    public String getJsonFileArray() {
        return jsonFileArray;
    }
    @Override
    public String toString() {
        return "{\n"+
                "\t\"onworker\":\""+super.onWorker+"\",\n"+
                "\t\"command\":\"shuffle\",\n"+
                "\t\"status\":\""+super.status+"\",\n"+
                "\t\"numOfReducers\":"+this.numOfReducers+",\n"+
                "\t\"outputPath\":\""+this.outputPath+"/\",\n"+
                "\t"+this.jsonFileArray.replace("{","").replace("}","")+"\n}";
    }
}
