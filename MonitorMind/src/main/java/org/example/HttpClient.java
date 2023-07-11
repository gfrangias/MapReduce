package org.example;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.Json;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;

public class HttpClient {
    public static String get(String endpointUrl) {
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    public static int post(String endpointUrl, String requestBody) {
        int statusCode = -1;

        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Set connection timeout
            connection.setConnectTimeout(1500);

            // Set read timeout
            connection.setReadTimeout(4500);

            if (requestBody != null) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
                outputStream.close();
            }

            statusCode = connection.getResponseCode();

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statusCode;
    }

    public static String getIdFromResponse(JsonArray jsonArray) {
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.getJsonObject(i);
                if (jsonObject.containsKey("Id")) {
                    return jsonObject.getString("Id");
                }
            }
        }
        return null;
    }

    public static String sendPostRequest(String urlString, JsonObject postData, String[] headers) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        for(String header : headers) {
            String[] parts = header.split(": ");
            conn.setRequestProperty(parts[0], parts[1]);
        }

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = postData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int http_status = conn.getResponseCode();
        if (http_status >= 200 && http_status < 300) {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        }
        return null;
    }

}