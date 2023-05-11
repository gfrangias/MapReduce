package org.example;

import org.apache.zookeeper.*;

import java.io.IOException;

public class ZooWatcher implements Watcher{
    private ZooKeeper zk;

    public ZooWatcher(String hostPort, int sessionTimeout) throws Exception {
        zk = new ZooKeeper(hostPort, sessionTimeout, this);
    }

    public ZooWatcher(ZooKeeper zk_arg){
        this.zk = zk_arg;
    }

    public void process(WatchedEvent event) {
        // Handle the event
        System.out.println("Received event: " + event.getType());
        // Add a new watch to the znode
        try {
            byte[] data = zk.getData(event.getPath(), this, null);
            System.out.println("Data: " + new String(data));
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void watchZNode(String znode) throws Exception {
        // Add a watch to the znode
        byte[] data = zk.getData(znode, this, null);
        System.out.println("Data: " + new String(data));
    }
}
