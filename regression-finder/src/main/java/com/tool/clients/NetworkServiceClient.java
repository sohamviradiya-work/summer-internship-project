package com.tool.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;


public abstract class NetworkServiceClient {
    
    protected void sendPostRequest(String endpoint, String requestBody) throws IOException, URISyntaxException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", getAuthHeader());
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = connection.getResponseCode();

        BufferedReader reader;
        if (status == HttpURLConnection.HTTP_CREATED) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorResponse.append(line);
            }
            reader.close();
            connection.disconnect();
            throw new IOException("HTTP request failed with status code " + status + ": " + errorResponse.toString());
        }
        
        connection.disconnect();
    }
    protected String sendGetRequest(String endpoint) throws IOException, URISyntaxException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getAuthHeader());
    
        int status = connection.getResponseCode();
    
        BufferedReader reader;
        if (status == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorResponse.append(line);
            }
            reader.close();
            connection.disconnect();
            throw new IOException("HTTP request failed with status code " + status + ": " + errorResponse.toString());
        }
    
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        return response.toString();
    }

    protected abstract String getAuthHeader() throws IOException, URISyntaxException;

}
