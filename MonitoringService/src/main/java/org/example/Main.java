package org.example;

import io.javalin.Javalin;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        System.out.println("Waiting for request to arrive...");
        app.get("/api/hello", ctx -> {
            ctx.result("Hello from Java application!");
            System.out.println("API --> Received GET request from " + ctx.ip() + " at endpoint /api/hello");
        });

        app.get("/api/user/145", ctx -> {
            ctx.result("Username: tasosmalakas");
            System.out.println("API --> Received GET request from " + ctx.ip() + " at endpoint /api/user/145");
        });

        app.get("/api/container/info", ctx -> {
            ctx.result("list of containers");
            System.out.println("API --> Received GET request from " + ctx.ip() + "at endpoint /api/container/info");
        });
        try{
            ZooKeeper zk = new ZooKeeper("192.168.1.105:2181", 10000, null);
            ZooWatcher zw = new ZooWatcher(zk);
            String znode = "/";
            ZNodeManager lister = new ZNodeManager(zk);
            zw.watchZNode(znode);
            lister.listZNodes(znode);
            Thread.sleep(Long.MAX_VALUE);
        }
        catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}