package org.example;
import io.javalin.Javalin;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.net.*;


public class Main {

    private static boolean zkAlive = false;
    private static final String[] zkAddresses = {"172.16.0.11:2181", "172.16.0.12:2181", "172.16.0.13:2181"};
    private static Random rng = new Random();

    public static void main(String[] args) {

        /*
         * Tha name of the container should be obtained during
         * its creation. For testing purposes uncomment the
         * second section.
         */
        String containerName = System.getenv("CONTAINER_NAME");
        if(System.getenv("JOB_ID")!=null){

        }
        //String containerName = "mjimy";
        String ipAddress = null;


        if (containerName == null) {
            throw new Error("Environment variable CONTAINER_NAME must be set!");
        }

        /*
            Find out what's my internal IP address to store it in my ephemeral
            znode
        */
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
            System.out.println("Internal IP Address:" + ipAddress);
        } catch (UnknownHostException e) {
            throw new Error("Unknown host exception");
        }

        //ipAddress = "192.168.1.6";

        /*
         If this code is run by the monitor 'fathermonitor' used
         during systems first deployment then it should wait a bit for
         the zookeeper ensemble to initialize, otherwise any further requests
         will fail.
         */
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

        /*
         * Select a ZK instance for connection. The connection
         * is performed towards a random ZK instance of the ensemble.
         */
        String zkAddress = Main.zkAddresses[rng.nextInt(Main.zkAddresses.length)];
        System.out.println("Will contact ZK instance at: " + zkAddress);

        /*
         * Init a container based web server for accepting requests
         * from others in MR network.
         */
        Javalin app = Javalin.create().start(7000);
        System.out.println("Waiting for requests to arrive...");


        /**
         * Set some API basic endpoints
         */
        app.get("/api/zk/status", ctx -> {
            ctx.result("ZK is active and connected: " + zkAlive);
        });

        app.get("/ping", ctx -> {
            ctx.result("1");
        });


        try {
            ZooKeeper zk = new ZooKeeper(zkAddress, 20000, null);
            zkAlive = true;
            ZNodeController zController = new ZNodeController(zk);
            //JobController jController = new JobController();

            //Register self into Zookeeper as monitor or fathermonitor (decided upon container name)
            zController.registerMe(containerName, ipAddress);

            //List all monitors
            app.get("/api/zk/monitors", ctx -> {
                ctx.result(zController.listAllZNodes("/monitors"));
                ctx.status(200); // Set the HTTP status code
            });

            //List all workers
            app.get("/api/zk/workers", ctx -> {
                ctx.result(zController.listAllZNodes("/workers"));
                ctx.status(200); // Set the HTTP status code
            });

            //List leader information
            app.get("/api/zk/leader", ctx -> {
                ctx.json(zController.getLeaderData()); // Set the response body to be the json containing leader info
                ctx.status(200); // Set the HTTP status code
            });

            //Watch node with id = id
            app.post("/api/zk/watchme/{id}", ctx -> {
                System.out.println("Will try to watch monitor znode with id:" + ctx.pathParam("id"));

                zController.watchNode("/monitors/"+ctx.pathParam("id")); // Make this monitor to watch the client requested attention

                System.out.println("I am successfully watching monitor znode with id:" + ctx.pathParam("id"));
                ctx.status(200); // Set the HTTP status code
            });

            //Handle Job with id = id
            app.post("/api/job/assign/{id}", ctx -> {
                if(!zController.iAmOccupied(containerName)){
                    System.out.println("Will handle job with id:"+ctx.pathParam("id"));
                    zController.makeMeOccupied(containerName);
                    ctx.status(200);
                }else{
                    ctx.status(503); //Unavailable if already committed to job
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}