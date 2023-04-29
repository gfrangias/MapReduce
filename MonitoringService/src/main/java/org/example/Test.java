package org.example;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        ZooKeeper zk = new ZooKeeper("192.168.1.105:2181", 5000, null);


    }
}
