package org.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;


import java.util.ArrayList;
import java.util.List;

public class ZNodeController implements Watcher {

    private ZooKeeper zk;
    public ZNodeController(ZooKeeper zk_arg){
        this.zk = zk_arg;
    }

    public void process(WatchedEvent event) {
        // Handle the event
        System.out.println("Received event: " + event.getType());
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            // The list of child nodes has changed
            try {
                List<String> children = zk.getChildren(event.getPath(), this);
                System.out.println("Child nodes: " + children);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void registerZnode(String znodeName, String data) throws Exception {
        Stat stat = this.zk.exists(znodeName, false); // Check if the ZNode already exists

        if (stat == null) {
            // Create a new ZNode
            byte[] byteData = data.getBytes();
            CreateMode createMode = CreateMode.PERSISTENT; // Set the create mode for the ZNode

            this.zk.create(znodeName, byteData, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
            System.out.println("ZNode created: " + znodeName);
        } else {
            System.out.println("ZNode already exists: " + znodeName);
        }
    }

    public void killZnode(String znodeName) throws Exception{

    }
    public String listAllZNodes(String znode) throws Exception {
        // Add a watch to the znode
        List<String> children = zk.getChildren(znode, this);
        return children.toString();
    }

    public String listMyData(String znodeName) throws Exception {
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
