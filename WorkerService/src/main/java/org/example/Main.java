package org.example;

import io.javalin.Javalin;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.net.*;


public class Main {

    private static boolean zkAlive = false;
    private static final String[] zkAddresses = {"172.16.0.11:2181", "172.16.0.12:2181", "172.16.0.13:2181"};
    private static Random rng = new Random();

    public static void main(String[] args) {

        String containerName = System.getenv("CONTAINER_NAME");
        if(containerName == null){
            throw new Error("Environment variable CONTAINER_NAME must be set!");
        }

        String ipAddress = null;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
            System.out.println("Internal IP Address: " + ipAddress);
        } catch (UnknownHostException e) {
            throw new Error("Unknown host exception");
        }
        if(containerName.contentEquals("fathermonitor")) {
            try {
                // Wait for 30 seconds
                Thread.sleep(30000);
                // Continue with the rest of your program
                System.out.println("Finished waiting for 30 seconds for ZK instances to initialize...");
            } catch (InterruptedException e) {
                throw new Error("Cannot wait for you baby...");
            }
        }

        String zkAddress = Main.zkAddresses[rng.nextInt(Main.zkAddresses.length)];

        System.out.println("Will contact ZK instance at: " + zkAddress);

        Javalin app = Javalin.create().start(7000);
        System.out.println("Waiting for requests to arrive...");


        app.get("/api/zk/status", ctx -> {
            ctx.result("ZK is active: " + zkAlive);
        });

        app.get("/ping", ctx -> {
            ctx.result("1");
        });



        try {
            ZooKeeper zk = new ZooKeeper(zkAddress, 20000, null);
            zkAlive = true;
            ZNodeController zController = new ZNodeController(zk);
            zController.registerZnode("/monitors", "");
            zController.registerZnode("/monitors/" + containerName, ipAddress);
            zController.registerZnode("/monitors/" + containerName + "/jobs", "reduce.jar");
            zController.registerZnode("/monitors/" + containerName + "/dataset", "file.csv");

            app.get("/api/zk/znodes/monitors", ctx -> {
                ctx.result(zController.listAllZNodes("/monitors"));
            });

            app.get("/api/zk/znodes/workers", ctx -> {
                ctx.result(zController.listAllZNodes("/workers"));
            });

            app.get("/api/zk/znode/mydata", ctx -> {
                ctx.result(zController.listMyData("/monitors/" + containerName));
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }







    }
}