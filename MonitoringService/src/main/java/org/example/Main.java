package org.example;

import io.javalin.Javalin;

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




    }
}