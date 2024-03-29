package model;

public class ReduceTask extends Task {
    private String intermediateFileInput;
    private String outputPath;
    private String functionCommand;
    private int index;

    public ReduceTask(String id, String cmd, String znodePath, TaskType type, String inFIn, String outP, int index, String functionCommand) {
        super(id, cmd, znodePath, type);
        this.intermediateFileInput = inFIn;
        this.outputPath = outP;
        this.index = index;
        this.functionCommand = functionCommand;
    }

    @Override
    public String toString() {
        return "{\n"+
                "\t\"onworker\":\""+super.onWorker+"\",\n"+
                "\t\"command\":\"reduce\",\n"+
                "\t\"status\":\""+super.status+"\",\n"+
                "\t\"fcommand\":\""+this.functionCommand+"\",\n"+
                "\t\"inputPath\":\""+this.intermediateFileInput+"\",\n"+
                "\t\"index\":\""+this.index+"\",\n"+
                "\t\"outputPath\":\""+this.outputPath+"/\"\n}";
    }
}
