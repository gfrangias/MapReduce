package model;

public class MapTask extends Task {

    private String functionName;
    private int index;

    public MapTask(String id, String worker, String znodePath, String cmd, TaskType type, int index, String fname) {
        super(id, worker, cmd, znodePath, type);
        this.functionName = fname;
        this.index = index;
    }

    public String getFunctionName() {
        return functionName;
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
                "\t\"index\":"+this.index+"\n}";
    }
}
