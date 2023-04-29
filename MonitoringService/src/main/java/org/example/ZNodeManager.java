package org.example;
import org.apache.zookeeper.*;

import java.util.List;


public class ZNodeManager implements Watcher {
    private ZooKeeper zk;

    public ZNodeManager(String hostPort, int sessionTimeout) throws Exception {
        zk = new ZooKeeper(hostPort, sessionTimeout, this);
    }

    public ZNodeManager(ZooKeeper zk_arg){
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

    public void listZNodes(String znode) throws Exception {
        // Add a watch to the znode
        List<String> children = zk.getChildren(znode, this);
        System.out.println("Child nodes: " + children);
    }
}
