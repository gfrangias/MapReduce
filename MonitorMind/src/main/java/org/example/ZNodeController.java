package org.example;

import model.Task;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import javax.json.*;
import java.util.*;

public class ZNodeController implements Watcher {

    private ZooKeeper zk;
    private String leadingMonitorIP;
    private String monitorName;
    private String electoralNodeName;

    private JobController jController;
    public ZNodeController(ZooKeeper zk_arg){
        this.zk = zk_arg;
    }
    public void giveJobControllerAccess(JobController j){
        this.jController = j;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("Got into process event");
        if (event.getType() == Event.EventType.NodeDeleted) {
            String deletedZNodePath = event.getPath();
            System.out.println("ZNode deleted: " + deletedZNodePath);
            // Perform further actions based on the deleted znode path
            if (deletedZNodePath.equals("/monitors/leader")) {
                // Should elect new leader, get into elections process
                try {
                    electLeader();
                } catch (Exception e) {}

            } else if (deletedZNodePath.startsWith("/workers")) {
                //Shit some worker failed, time to see what it was doing and reassign its job
                System.out.println("Worker "+deletedZNodePath + "failed. RIP...");
                try{
                    jController.handleWorkerFailure(deletedZNodePath);
                } catch(Exception e){
                    //Handle failure during reassignment
                    e.printStackTrace();
                }
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
        this.monitorName = znodeName;

        /*
         * 2 cases:
         * -Register the fathermonitor
         * -Register a simple monitor
         */
        if(znodeName.contentEquals("fathermonitor")) {

            //Init the /monitors node if not exists
            Stat stat = this.zk.exists("/monitors", false);
            if (stat == null) {
                System.out.println("As a fathermonitor I will init the znode of monitors...");
                registerPersistentZnode("/monitors", "");
            }

            //Init the /elections znode if not exists
            stat = this.zk.exists("/elections", false);
            if (stat == null) {
                System.out.println("As a fathermonitor I will init the znode of elections...");
                registerPersistentZnode("/elections", "");
            }

            //Register the leader to its dedicated znode
            registerEphemeralZnode("/monitors/leader", znodeName);

            //Register it also as a plain monitor. Leader though is not considered to handle jobs
            //so set occupied to 1 forever.
            data = "{\"ipAddress\":\""+ myIP +"\", \"occupied\":true}";
            registerEphemeralZnode("/monitors/"+znodeName, data);

        }else{
            //Register plain monitor
            data = "{\"ipAddress\":\""+ myIP +"\", \"occupied\":false}";
            registerEphemeralZnode("/monitors/"+znodeName, data);

            //No leader present case
            if(zk.exists("/monitors/leader", false) == null){
                //Check maybe if leader failed and I entered the system during the election process
                HashMap<String, String> map = listChildrenData("/elections");
                if(map.isEmpty()){
                    //Then no candidates, I become leader!!!
                    //Register the leader to its dedicated znode
                    registerEphemeralZnode("/monitors/leader", znodeName);
                    return;
                }
            }

            //Should create my node for possible elections in the future
            this.electoralNodeName = registerElectoralNode();
            System.out.println("I register my electoral znode in: "+this.electoralNodeName);

            //If I am a plain monitor then the fathermonitor should watch me for crashes
            //and I should watch him for crashes too.
            String url = "http://"+getLeadingMonitorIP()+":7000/api/zk/watchme/"+znodeName;
            //Ask the leader to watch me
            HttpClient.post(url,null);
            //And I will watch the leader

            watchNode("/monitors/leader");
        }
    }

    public void makeMeAvailable(String znodeName) throws Exception {
        try {
            JsonObject currData = getMonitorData(znodeName);
            String monIp = currData.getString("ipAddress");
            String newData = "{\"ipAddress\":\"" + monIp + "\", \"occupied\":false}";
            zk.setData("/monitors/" + znodeName, newData.getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void makeMeOccupied(String znodeName) {
        try {
            JsonObject currData = getMonitorData(znodeName);
            String monIp = currData.getString("ipAddress");
            String newData = "{\"ipAddress\":\"" + monIp + "\", \"occupied\":true}";
            zk.setData("/monitors/" + znodeName, newData.getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean iAmOccupied(String znodeName) {
        try {
            boolean status = getMonitorData(znodeName).getBoolean("occupied");
            return status;
        } catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Make this monitor a watcher for a specified znode name in /monitors/
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

    /**
     * Make this monitor a non watcher for a specified znode name
     * @param znodePath
     */
    public void unwatchNode(String znodePath) throws Exception {
        // Check if the znode exists
        Stat stat = zk.exists(znodePath, false);

        if (stat != null) {
            // Stop watching the znode for changes
            zk.getData(znodePath, false, null);
        } else {
            System.out.println("ZNode does not exist.");
        }
    }

    /**
     * Returns a NON-COMPLETED task of a worker for a job
     * Used mainly when detecting worker failures
     * @param workerID:  the id of the failed worker
     * @param jobZnode: the znode path of the job
     * @returns List of Strings with task IDs
     */
    public HashMap<String, String> getNonCompletedTasksOfFailedWorkerByJob(String workerID, String jobZnode) throws Exception {

        //Get the assignment of tasks to workers
        HashMap<String, String> tasksAndWorkers = listChildrenData(jobZnode+"/assignments");

        //Remove any tasks assigned to other workers other than the one for which we are handling the failure now
        removeNotHavingValue(tasksAndWorkers, workerID);

        //Do not want to handle any completed tasks
        //For every task remaining in the HashMap check if its completed. If it is, then throw it away
        for(String s: tasksAndWorkers.keySet()){
            JsonObject t = getTaskData(jobZnode+"/tasks/"+s);
            //Throw away if task is marked completed
            if(t.getString("status").equals("COMPLETED")){
                tasksAndWorkers.remove(s);
            }
        }

        return tasksAndWorkers;
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
     * Removees
     * @param map
     * @param value
     * @param <K> Generic Type | any
     * @param <V> Generic Type | any
     */
    public static <K, V> void removeNotHavingValue(HashMap<K, V> map, V value) {
        Iterator<HashMap.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<K, V> entry = iterator.next();
            if (!entry.getValue().equals(value)) {
                iterator.remove();
            }
        }
    }


    /**
     * Used for registering an electoral znode in zk for the current
     * monitor. The znode created will be used in case the leader monitor
     * fails. The znodes created are sequential and every
     * node receives a name based on the sequence it entered the electoral
     * znode /elections.
     * @return The path of the electoral node of this monitor
     */
    public String registerElectoralNode() throws Exception {
        String pathWithSequence = zk.create("/elections/cand_",new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        return pathWithSequence;
    }

    /**
     * It 'pops' the minimum electoral node from /elections znode to
     * decide the new leader in case the previous leader is dead.
     */
    public void electLeader() throws Exception {
        List<String> children = zk.getChildren("/elections", false);
        Collections.sort(children);
        String smallestChild = "/elections/"+children.get(0);

        if(smallestChild.equals(this.electoralNodeName)) {
            System.out.println("I am the leader");
            //Reserve leader zNode
            registerEphemeralZnode("/monitors/leader", this.monitorName);
            acquireLeadershipRights("/monitors");
        }else{
            //Sadly not my turn yet to become leader. I must watch the new leader then
            System.out.println("Habemus Papam!");
            System.out.println("Waiting for him to tell me to watch him...");
        }
    }

    /**
     * Make the new elected leader to watch the other monitors as
     * long as this is the job of the leader.
     */
    public void acquireLeadershipRights(String znodeParentDir) throws Exception {
        List<String> children = zk.getChildren(znodeParentDir, this);
        // Watch each child node
        for (String child : children) {
            //Don't want to monitor leader (it's me) and eventually a zombie znode named fathermonitor
            if (child.equals("leader") || child.equals("fathermonitor") || child.equals(this.monitorName)) {
                continue; // Skip watching this child
            }
            String childPath = znodeParentDir + "/" + child;
            zk.exists(childPath, this);
        }
        JsonObject json = null;
        String url = null;

        //I should ask them to watch me when I am ready to be watched
        for (String child : children) {
            //Don't want to monitor leader (it's me) and eventually a zombie znode named fathermonitor
            //or my natural znode
            if (child.equals("leader") || child.equals("fathermonitor") || child.equals(this.monitorName)) {
                continue; // Skip watching this child
            }
            json = getMonitorData(child);
            url = "http://"+json.getString("ipAddress")+":7000/api/zk/watchme/leader";
            //Ask my child to watch me
            HttpClient.post(url,null);
        }

        System.out.println("I acquired leadership responsibility of watching all child monitors. I am the king!");
        //If the monitor that became leader had a job previously, his job should be reassigned
        //to a new monitor because leader is not supposed to handle jobs.
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


    public void insertTaskToZK(Task t) throws Exception {
        String data = t.toString();
        registerPersistentZnode(t.getZnodePath(),data);
        System.out.println("Registered task: "+t.getZnodePath() + " to ZK");
    }


    public List<String> getTasksOfJob(String znode) throws Exception {
        List<String> childrenTasks = zk.getChildren(znode+ "/tasks", null);
        return childrenTasks;
    }

    public void insertAssignmentToZK(String jobPath, String t, String worker) throws Exception{
        String aPath = "/jobs/"+jobPath + "/assignments/" + t;
        //If assignment was previously done then an exception will occur if we try to overwrite it
        //If exists update it, if not write it
        if(zk.exists(aPath, false) == null){
            registerPersistentZnode(aPath, worker);
        }else{
            zk.setData(aPath, worker.getBytes(), -1);
        }
        System.out.println("Updated assignment of task set to worker: "+ worker);
    }

    public void updateWorkerOfTask(String znodePath, String worker) throws Exception {
        try {
            JsonObject currData = getZnodeData(znodePath);
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(currData);
            jsonObjectBuilder.add("onworker",worker);
            JsonObject modifiedJsonObject = jsonObjectBuilder.build();
            zk.setData(znodePath, Jsonizer.jsonObjectToString(modifiedJsonObject).getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Updates the znode of statistics for given stage
     * @param jobZnode
     * @stage the stage to update stats for
     * @time total execution time for stage
     * @workers total worker assignments needed
     */
    public void updateStatsOfStage(String jobZnode, String stage, double time, int workers) throws Exception {
        String data = "{\n\"workers\":"+ workers +",\n \"time\":"+time+"\n}";
        zk.setData("/jobs/"+jobZnode+"/statistics/"+stage, data.getBytes(), -1);
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
     * Returns all children znodes of persistent znode specified.
     * @return a String with all childrens of the znode workers
     */
    public List<String> getAllIdleWorkers() throws Exception {
        List<String> children = zk.getChildren("/workers", null);
        List<String> idleChildren = new ArrayList<String>();
        for(String s : children){
            JsonObject w = getWorkerData(s);
            if(w.getString("status").equals("idle")){
                idleChildren.add(s);
            }
        }
        return idleChildren;
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
     * Returns the information of a node data based on the node name
     * retrieving it from /workers/$znodename$
     * @return a JsonObject object containing the information of the node
     */
    public JsonObject getWorkerData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData("/workers/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Returns the information of a node data based on the node name
     * retrieving it from /jobs/$znodename$
     * @return a JsonObject object containing the information of the node
     */
    public JsonObject getJobData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData("/jobs/"+znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    /**
     * Returns the data of a specific znode
     * @param znode
     * @return data in the znode
     * @throws Exception
     */
    public JsonObject getZnodeData(String znode) throws Exception {
        String nodeInfo = new String(zk.getData(znode, null, null));
        JsonObject jsonObj = Jsonizer.jsonStringToObject(nodeInfo);
        return jsonObj;
    }

    public void updateJobStatus(String znode, String status){
        try {
            JsonObject currData = getJobData(znode);
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(currData);
            jsonObjectBuilder.add("status",status);
            JsonObject modifiedJsonObject = jsonObjectBuilder.build();
            zk.setData("/jobs/" + znode, Jsonizer.jsonObjectToString(modifiedJsonObject).getBytes(), -1);
        }catch(Exception e){
            e.printStackTrace();
        }
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

    /**
     * @return The name of the monitor currently in /monitors/leader
     * @throws Exception
     */
    public String getLeaderName() throws Exception {
        String fatherName = new String(zk.getData("/monitors/leader", null, null));
        return fatherName;
    }

    /**
     * @return The IP address of the leading monitor inside the map_reduce_network
     * @throws Exception
     */
    public String getLeadingMonitorIP() throws Exception {
        leadingMonitorIP = getLeaderData().getString("ipAddress");
        return leadingMonitorIP;
    }

    /**
     * Return all alive workers in the system based on their registrations in ZK
     * This
     */
    //public ArrayList<String>

    public HashMap<String, String> listChildrenData(String znodeName) throws Exception {
        // Add a watch to the znode
        // Get the children (subnodes) of the znode
        List<String> children = zk.getChildren(znodeName, this);

        // Append the data of the znode and its subnodes to a list
        HashMap<String, String> childrenAndValues = new HashMap<String, String>();

        for (String child : children) {
            String childPath = znodeName + "/" + child;
            byte[] childData = zk.getData(childPath, this, null);
            childrenAndValues.put(child, new String(childData));
        }

        // Return the list as a string using toString
        return childrenAndValues;
    }

}
