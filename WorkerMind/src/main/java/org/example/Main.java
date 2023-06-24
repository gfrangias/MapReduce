package org.example;

import io.javalin.Javalin;
import org.apache.zookeeper.ZooKeeper;
import java.util.concurrent.CompletableFuture;
import java.util.Random;
import java.net.*;


public class Main {

    private static boolean zkAlive = false;
    private static final String[] zkAddresses = {"172.16.0.11:2181", "172.16.0.12:2181", "172.16.0.13:2181"};
    private static Random rng = new Random();

    private static String dedicatedToMonitor;
    private static String containerName;

    public static void main(String[] args) {

        containerName = System.getenv("CONTAINER_NAME");
        dedicatedToMonitor = System.getenv("DEDICATED_TO");

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
        System.out.println("Will contact ZK instance at: " + zkAddress);

        Javalin app = Javalin.create().start(6000);
        System.out.println("Waiting for requests to arrive...");

        app.get("/api/zk/status", ctx -> {
            ctx.result("ZK is active: " + zkAlive);
        });

        app.get("/ping", ctx -> {
            ctx.result("1");
            ctx.status(200);
        });


        try {
            ZooKeeper zk = new ZooKeeper(zkAddress, 20000, null);
            zkAlive = true;
            ZNodeController zController = new ZNodeController(zk);
            TaskController tController = new TaskController(zController);
            zController.registerMe(containerName, ipAddress, dedicatedToMonitor);
            if(dedicatedToMonitor.equals("none")){
                zController.makeMeIdle(containerName);
            }
            app.get("/api/status", ctx -> {
                ctx.result(zController.getWorkerStatus(containerName));
                ctx.status(200); // Set the HTTP status code
            });

            //Handle Task with id = id when receiving request to do so
            app.post("/api/task/assign", ctx -> {
                if(zController.iAmReserved(containerName)){

                    //Check if assignment is coming from the monitor that deployed me or reserved me.
                    String monitorName = ctx.queryParam("monitor");
                    String tid = ctx.queryParam("tid");

                    if(monitorName.equals(dedicatedToMonitor)){
                        if(zController.iAmWorking(containerName)){
                            ctx.status(503);
                        }
                        zController.makeMeWorking(containerName);
                        System.out.println("Will handle task with task znode path: "+tid+" for monitor: "+monitorName);
                        ctx.status(200);
                        /*
                            The task of the user will be completed async to the acknowledgment that it will be handled
                            by the worker the monitor assigned it to.
                        */
                        CompletableFuture.runAsync(() -> {
                            try {
                                tController.handleTask(tid,containerName);
                                // Handle any logic if the function executes successfully
                            } catch (Exception e) {
                                // Handle exception
                                e.printStackTrace();
                            }
                        });
                    }else{
                        ctx.status(503); //Not your worker
                    }
                }else{
                    ctx.status(503); //Unavailable if already committed to job
                }
            });

            app.post("/api/reserve/{monitor}", ctx -> {
                if(zController.iAmIdle(containerName)){
                    zController.makeMeReserved(containerName);
                    dedicatedToMonitor = ctx.pathParam("monitor");
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