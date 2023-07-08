package org.example;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DockerController {

    private static final String DOCKER_API_URL = "http://172.0.17.1:2375";

    public static void deployWorker(String containerName, String dedicatedTo) throws Exception {
        String imageName = "zooopsworker";
        JsonObject hostConfig = Json.createObjectBuilder()
                .add("NetworkMode", "map_reduce_net")
                .add("Binds", Json.createArrayBuilder().add("/home/vsam/zooops/MapReduce/docker_env/filedb/uploads:/app/uploads"))
                .build();

        JsonObject postData = Json.createObjectBuilder()
                .add("Image", imageName)
                .add("HostConfig", hostConfig)
                .add("Env", Json.createArrayBuilder()
                        .add("CONTAINER_NAME=" + containerName)
                        .add("DEDICATED_TO=" + dedicatedTo))
                .build();
        String[] headers = {"Content-Type: application/json", "Content-Length: " + postData.toString().length()};

        try {
            String response = HttpClient.sendPostRequest("http://172.17.0.1:2375/containers/create", postData, headers);
            if (response != null) {
                JsonReader jsonReader = Json.createReader(new StringReader(response));
                JsonObject responseObject = jsonReader.readObject();
                jsonReader.close();
                String container_id = responseObject.getString("Id");

                //Rename the container
                JsonObject renameData = Json.createObjectBuilder().add("name", containerName).build();
                HttpClient.sendPostRequest("http://172.17.0.1:2375/containers/" + container_id + "/rename?name=" + containerName, renameData, headers);

                //Start the container
                String start_response = HttpClient.sendPostRequest("http://172.17.0.1:2375/containers/" + container_id + "/start", Json.createObjectBuilder().build(), new String[]{"Content-Type: application/json"});

                if (start_response == null) {
                    System.out.println("Error starting container: " + start_response);
                }
            } else {
                System.out.println("Error creating container: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(containerName);
    }
}