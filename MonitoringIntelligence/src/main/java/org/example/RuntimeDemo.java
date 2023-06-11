package org.example;

import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RuntimeDemo {

    public static void main(String[] args) {
        try {

            Javalin app = Javalin.create().start(7000);
            System.out.println("Waiting for requests to arrive...");

            // print a message
            System.out.println("Executing notepad.exe");

            // create a process and execute notepad.exe
            Process process = Runtime.getRuntime().exec("java --version");
            // Get the input stream of the process
            InputStream inputStream = process.getInputStream();

            // Wrap the input stream in a BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read the output line by line
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // print another message
            System.out.println("Notepad should now open.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}