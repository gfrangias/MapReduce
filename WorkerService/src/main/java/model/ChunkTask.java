package model;

public class ChunkTask extends Task{
    private int numOfChunks;
    private long offSetAtFile;
    private String datasetPath;
    private long chunkSize;

    public ChunkTask(String id, String onWorker, String cmd, String znodePath, TaskType type, String dp, int noc, long offs, long csz){
        super(id, onWorker, cmd, znodePath, type);
        this.numOfChunks = noc;
        this.datasetPath = dp;
        this.offSetAtFile = offs;
        this.chunkSize = csz;
    }
    public int getNumOfChunks() {
        return numOfChunks;
    }

    public long getOffSetAtFile() {
        return offSetAtFile;
    }

    public String getDatasetPath() {
        return datasetPath;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    @Override
    public String toString(){
        return "{\n"+
                "\t\"onworker\":\""+super.onWorker+"\",\n"+
                "\t\"command\":\"chunk\",\n"+
                "\t\"status\":\""+super.status+"\",\n"+
                "\t\"dataset\":\""+this.datasetPath+"\",\n"+
                "\t\"offset\":"+this.offSetAtFile+",\n"+
                "\t\"numOfChunks\":"+this.numOfChunks+",\n"+
                "\t\"chunkSize\":"+this.chunkSize+"\n}";
    }
}
