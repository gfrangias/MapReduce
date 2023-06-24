package model;

public class MergeTask extends Task{
    private String outputPath;
    private String jsonFileArray;

    public MergeTask(String id, String cmd, String znodePath, TaskType type, String outP, String jsonFileArray) {
        super(id, cmd, znodePath, type);
        this.outputPath = outP;
        this.jsonFileArray = jsonFileArray;
    }
    @Override
    public String toString() {
        return "{\n"+
                "\t\"onworker\":\""+super.onWorker+"\",\n"+
                "\t\"command\":\"merge\",\n"+
                "\t\"status\":\""+super.status+"\",\n"+
                "\t\"outputPath\":\""+this.outputPath+"/\",\n"+
                "\t"+this.jsonFileArray.replace("{","").replace("}","")+"\n}";
    }
}
