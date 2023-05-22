package org.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import javax.json.*;

import java.util.ArrayList;
import java.util.List;

public class ZNodeController implements Watcher {

    private ZooKeeper zk;
    private String leadingMonitorIP;
    public ZNodeController(ZooKeeper zk_arg){
        this.zk = zk_arg;
    }


    public String getLeadingMonitorIP() throws Exception {
        leadingMonitorIP = getLeaderData().getString("ipAddress");
        return "192.168.1.105";
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            String deletedZNodePath = event.getPath();
            System.out.println("ZNode deleted: " + deletedZNodePath);
            // Perform further actions based on the deleted znode path
            if (deletedZNodePath.equals("/monitors/leader")) {
                // Should elect new leader, get into elections process
                electLeader();
            } else if (deletedZNodePath.startsWith("/workers")) {
                //Some worker failed, time to see what it was doing and reassign its job
                handleWorkerFailure(deletedZNodePath);
            }
        }
    }

    /**
     * Registers a znode as a monitor inside the ZK. If a 'fathermonitor' name
     * is given to the monitor then the function will create znodes making this
     * monitor the leading one.
     * @param znodeName
     * @param myIP
     */
    public void registerMe(String znodeName, String myIP) throws Exception {

        String data;

        /*
         * 2 cases:
         * -Register the fathermonitor
         * -Register a simple monitor
         */
        if(znodeName.contentEquals("fathermonitor")){

            //Init the /monitors node if not exists
            Stat stat = this.zk.exists("/monitors", false);
            if (stat == null) {
                System.out.println("As a fathermonitor I will init the znode of monitors...");
                registerPersistentZnode("/monitors", "");
            }

            //Register the leader to its dedicated znode
            registerEphemeralZnode("/monitors/leader", znodeName);

            //Register it also as a plain monitor. Leader though is not able to handle jobs
            //so set occupied to 1 forever.
            data = "{\"ipAddress\":\""+ myIP +"\", \"occupied\":1, \"leaderIP\":\""+myIP+"\"}";
            registerEphemeralZnode("/monitors/"+znodeName, data);

        }else{
            //Register plain monitor
            data = "{\"ipAddress\":\""+ myIP +"\", \"occupied\":0, \"leaderIP\":\""+getLeadingMonitorIP()+"\"}";
            registerEphemeralZnode("/monitors/"+znodeName, data);

            //If I am a plain monitor then the fathermonitor should watch me for crashes
            //and I should watch him for crashes too.
            String url = "http://"+getLeadingMonitorIP()+":7000/api/zk/watchme/"+znodeName;
            //Ask the leader to watch me
            HttpClient.post(url,null);
            //And I will watch the leader
            watchNode("/monitors/"+getLeaderName());
        }
    }


    public void makeMeAvailable(String znodeName) throws Exception {

    }

    public void makeMeOccupied(String znodeName) throws Exception {

    }

    public boolean iAmOccupied(String znodeName) throws Exception {
        boolean status = getMonitorData(znodeName).getBoolean("occupied");
        return status;
    }

    /**
     * Make this monitor a watcher for a specified znode name in /monitors/
     * @param znodeName
     */
    public void watchNode(String znodeName) throws Exception {
        // Check if the znode exists
        Stat stat = zk.exists("/monitors/"+znodeName, false);

        if (stat != null) {
            // Start watching the znode for changes
            zk.getData("/monitors/"+znodeName, true, null);
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


    public void electLeader(){

    }

    public void handleWorkerFailure(String znodePath){

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
    public JsonObject getMonitorData(String znode) throws Exception{
        String nodeInfo = new String(zk.getData("/monitors/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }


    /**
     * Returns the information of a node data based on the node name
     * retrieving it from /jobs/$znodename$
     * @return a JsonObject object containing the information of the node
     */
    public JsonObject getJobData(String znode) throws Exception{
        String nodeInfo = new String(zk.getData("/jobs/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Returns the information of the leader data based on the leader name
     * currently living in znode "/monitors/leader". The info of
     * the father monitor is retrieved from /monitors/$name$ node,
     * @return a JsonObject object containing the information of the leader
     */
    public JsonObject getLeaderData() throws Exception {
        // Add a watch to the znode
        String fatherName = new String(zk.getData("/monitors/leader", null, null));
        String fatherInfo = new String(zk.getData("/monitors/"+fatherName, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(fatherInfo);
        return jsonObj;
    }

    public String getLeaderName() throws Exception{
        String fatherName = new String(zk.getData("/monitors/leader", null, null));
        return fatherName;
    }

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
