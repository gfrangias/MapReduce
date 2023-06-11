package model;

import java.util.ArrayList;

public class MapTask extends Task {

    public MapTask(String id, String cmd, String znodePath, TaskType type, ArrayList<Integer> indexes) {
        super(id, cmd, znodePath, type);
    }

    @Override
    public String toString() {
        return "";
    }
    
}
