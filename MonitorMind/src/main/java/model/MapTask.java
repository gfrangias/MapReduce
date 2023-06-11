package model;

public class MapTask extends Task {

    private String functionName;
    private String inputFilePath;
    private String outputFilePath;
    private int index;

    public MapTask(String id, String znodePath, TaskType type, int index, String fname, String in, String out) {
        super(id, "", znodePath, type);

        this.functionName = fname;
        this.index = index;
        this.inputFilePath = in;
        this.outputFilePath = in;

        super.command = new String("python3 " + fname + " < " + in + " > " + out + "/map_" + index + ".intermediate");
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public int getIndex() {
        return index;
    }
    public void assignToWorker(String workerName){
        this.onWorker = workerName;
    }

    @Override
    public String toString(){
        return "{\n"+
                "\t\"onworker\":\""+super.onWorker+"\",\n"+
                "\t\"command\":\"map\",\n"+
                "\t\"function\":\""+this.functionName+"\",\n"+
                "\t\"status\":\""+super.status+"\",\n"+
                "\t\"inputfile\":\""+this.inputFilePath+"\",\n"+
                "\t\"index\":"+this.index+",\n"+
                "\t\"outputfile\":\""+this.outputFilePath+"\\n}";
    }
}
