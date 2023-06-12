package org.example;

import model.TaskStatus;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;

public class ZNodeController implements Watcher {

    private ZooKeeper zk;
    private String myMonitor;
    public ZNodeController(ZooKeeper zk_arg){
        this.zk = zk_arg;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            String deletedZNodePath = event.getPath();
            System.out.println("ZNode deleted: " + deletedZNodePath);
            // Perform further actions based on the deleted znode path
        }
    }

    /**
     * Registers worker (self) in ZK and asks monitor to which it is dedicated to watch him
     * @param znodeName
     * @param myIP
     * @param myMonitorName
     * @throws Exception
     */
    public void registerMe(String znodeName, String myIP, String myMonitorName) throws Exception {
        // Store the monitor name locally
        this.myMonitor = myMonitorName;
        String data;


        //Init the /workers node if not exists
        Stat stat = this.zk.exists("/workers", false);
        if (stat == null) {
            System.out.println("Init /workers node because it didnt exist...");
            registerPersistentZnode("/workers", "");
        }

        // Register worker in ZK
        data = "{\"ipAddress\":\""+ myIP +"\",\"taskpath\":\"empty\", \"status\":\"reserved\"}";
        registerEphemeralZnode("/workers/"+znodeName, data);
        System.out.println("Registered myself in ZK");

        if(!myMonitorName.equals("none")) {
            //I should ask my monitor to watch me in case I fail
            String myMonitorIP = getMyMonitorIP(myMonitorName);
            String url = "http://" + getMyMonitorIP(myMonitorName) + ":7000/api/zk/watchmeasworker/" + znodeName;
            //Ask the monitor to watch me
            HttpClient.post(url, null);
            System.out.println("The monitor who brought me up is watching me now");
            //And I will watch the monitor
            watchNode("/monitors/" + myMonitorName);
            System.out.println("I am gonna watch the monitor who brought me up");
        }
    }

    public String getWorkerStatus(String znodeName){
        try {
            JsonObject currData = getWorkerData(znodeName);
            String status = currData.getString("status");
            return status;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean iAmIdle(String znodeName){
        try {
            JsonObject currData = getWorkerData(znodeName);
            String status = currData.getString("status");
            if(status.equals("idle")){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean iAmReserved(String znodeName){
        try {
            JsonObject currData = getWorkerData(znodeName);
            String status = currData.getString("status");
            if(status.equals("reserved")){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean iAmWorking(String znodeName){
        try {
            JsonObject currData = getWorkerData(znodeName);
            String status = currData.getString("status");
            if(status.equals("working")){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void makeMeReserved(String znodeName) {
        try {
            JsonObject currData = getWorkerData(znodeName);
            String worIp = currData.getString("ipAddress");
            String newData = "{\"ipAddress\":\"" + worIp + "\", \"taskpath\":\"empty\", \"status\":\"reserved\"}";
            zk.setData("/workers/" + znodeName, newData.getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void makeMeWorking(String znodeName) {
        try {
            JsonObject currData = getWorkerData(znodeName);
            String monIp = currData.getString("ipAddress");
            String taskPath = currData.getString("taskpath");
            String newData = "{\"ipAddress\":\"" + monIp + "\", \"taskpath\": \""+taskPath+"\",\"status\":\"working\"}";
            zk.setData("/workers/" + znodeName, newData.getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void makeMeIdle(String znodeName) {
        try {
            JsonObject currData = getWorkerData(znodeName);
            String worIp = currData.getString("ipAddress");
            String newData = "{\"ipAddress\":\"" + worIp + "\", \"taskpath\": \"empty\",\"status\":\"idle\"}";
            zk.setData("/workers/" + znodeName, newData.getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void storeChunkFilePath(){

    }
    /**
     * Make this worker a watcher for a specified znode name in ZK
     * @param znodePath
     */
    public void watchNode(String znodePath) throws Exception {
        // Check if the znode exists
        Stat stat = zk.exists(znodePath, this);

        if (stat != null) {
            // Start watching the znode for changes
            zk.getData(znodePath, this, null);
        } else {
            System.out.println("ZNode does not exist.");
        }
    }

    public void registerEphemeralZnode(String znodeName, String data) throws Exception {
        Stat stat = this.zk.exists(znodeName, false); // Check if the ZNode already exists

        if (stat == null) {
            // Create a new ZNode
            byte[] byteData = data.getBytes();
            CreateMode createMode = CreateMode.EPHEMERAL; // Set the create mode for the ZNode

            this.zk.create(znodeName, byteData, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
            this.zk.exists(znodeName, this);
            System.out.println("ZNode created: " + znodeName);
        } else {
            System.out.println("ZNode already exists: " + znodeName);
        }
    }

    public void registerPersistentZnode(String znodeName, String data) throws Exception {
        Stat stat = this.zk.exists(znodeName, false); // Check if the ZNode already exists

        if (stat == null) {
            // Create a new ZNode
            byte[] byteData = data.getBytes();
            CreateMode createMode = CreateMode.PERSISTENT; // Set the create mode for the ZNode

            this.zk.create(znodeName, byteData, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
            this.zk.exists(znodeName, this);
            System.out.println("ZNode created: " + znodeName);
        } else {
            System.out.println("ZNode already exists: " + znodeName);
        }
    }

    /**
     * Returns all children znodes of persistent znode specified.
     * @param znode the Znode of which the childrens are requested
     * @return a String with all childrens of the znode
     */
    public String listAllZNodes(String znode) throws Exception {
        List<String> children = zk.getChildren(znode, null);
        return children.toString();
    }

    /**
     * Returns the information of a node data based on the node name
     * retrieving it from /monitors/$znodename$
     * @return a JsonObject object containing the information of the node
     */
    public JsonObject getMonitorData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData("/monitors/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Return only the internal IP of the monitor that called this worker to be born
     * @param monitorName
     * @return the monitorIp given by registerMe as acquired from the ENVIRONMENT VARIABLE
     * @throws Exception
     */
    public String getMyMonitorIP(String monitorName) throws Exception {
        JsonObject obj = getMonitorData(monitorName);
        return obj.getString("ipAddress");
    }

    /**
     * Returns the information of a node data based on the node name
     * retrieving it from /monitors/$znodename$
     * @return a JsonObject object containing the information of the node
     */
    public JsonObject getWorkerData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData("/workers/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Returns the information of a task of a specific job.
     * @param znode the znode path of the task.
     * @return a JsonObject object containing the information of the task
     */
    public JsonObject getTaskData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData(znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Updates the 'status' parameter of the json of a task.
     * @param znode the znode path for the corresponding task
     * @param status some status from the enum TaskStatus
     */
    public void updateTaskStatus(String znode, TaskStatus status){
        try {
            JsonObject currData = getTaskData(znode);
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(currData);
            jsonObjectBuilder.add("status", status.toString());
            JsonObject modifiedJsonObject = jsonObjectBuilder.build();
            zk.setData(znode, Jsonizer.jsonObjectToString(modifiedJsonObject).getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void writeTaskResult(String znode, String result) throws Exception {
        //Init the /workers node if not exists
        Stat stat = this.zk.exists("/workers", false);
        if (stat == null) {
            System.out.println("Init /workers node because it didnt exist...");
            registerPersistentZnode("/workers", "");
        }
    }

    /**
     * Return all alive workers in the system based on their registrations in ZK
     * This
     */
    //public ArrayList<String>

    public String listChildrenData(String znodeName) throws Exception {
        // Add a watch to the znode
        // Get the children (subnodes) of the znode
        List<String> children = zk.getChildren(znodeName, this);

        // Append the data of the znode and its subnodes to a list
        List<String> dataStrings = new ArrayList<String>();

        byte[] data = zk.getData(znodeName, this, null);
        dataStrings.add(new String(data));

        for (String child : children) {
            String childPath = znodeName + "/" + child;
            byte[] childData = zk.getData(childPath, this, null);
            dataStrings.add(child + ":" + new String(childData));
        }

        // Return the list as a string using toString
        return dataStrings.toString();
    }

}
