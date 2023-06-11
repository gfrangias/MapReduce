package org.example;

import io.javalin.Javalin;
import io.javalin.http.servlet.Task;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

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
        String dedicatedToMonitor = System.getenv("DEDICATED_T0");


        if(containerName == null){
            throw new Error("Environment variable CONTAINER_NAME must be set!");
        }

        if(dedicatedToMonitor == null){
            throw new Error("Environment variable DEDICATED_TO must be set!");
        }
        String ipAddress = null;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
            System.out.println("Internal IP Address: " + ipAddress);
        } catch (UnknownHostException e) {
            throw new Error("Unknown host exception");
        }

        String zkAddress = Main.zkAddresses[rng.nextInt(Main.zkAddresses.length)];
        zkAddress = "192.168.1.105:2181";

        System.out.println("Will contact ZK instance at: " + zkAddress);

        Javalin app = Javalin.create().start(6000);
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
            TaskController tController = new TaskController(zController);
            zController.registerMe(containerName, ipAddress, dedicatedToMonitor);

            app.get("/api/occupied", ctx -> {
                ctx.result(String.valueOf(zController.iAmOccupied(containerName)));
                ctx.status(200); // Set the HTTP status code
            });

            //Handle Task with id = id when receiving request to do so
            app.post("/api/task/assign", ctx -> {
                if(!zController.iAmOccupied(containerName)){
                    //Check if assignment is coming from the monitor that deployed me
                    String monitorName = ctx.queryParam("monitor");
                    String tid = ctx.queryParam("tid");
                    if(monitorName.equals(dedicatedToMonitor)){
                        System.out.println("Will handle task with task znode path: "+tid+" for monitor: "+monitorName);
                        tController.handleTask(tid);
                        zController.makeMeOccupied(containerName);
                        ctx.status(200);
                    }else{
                        ctx.status(503); //Not your worker
                    }
                }else{
                    ctx.status(503); //Unavailable if already committed to job
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}