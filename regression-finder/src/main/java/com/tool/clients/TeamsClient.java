package com.tool.clients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.items.TeamsNotification;
import com.tool.Config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class TeamsClient extends NetworkServiceClient {

    private final HashMap<String, String> emailMap;
    private String accessToken;

    private TeamsClient(String accessToken) {
        this.emailMap = new HashMap<>();
        emailMap.put("sohamviradiya.work@gmail.com", "soham.viradiya@sprinklr.com");
        this.accessToken = accessToken;
    }

    public void sendNotification(TeamsNotification teamsNotification) throws IOException {

        String userId = "";

        String endPoint = "";

        String payload = sendProactiveMessage(teamsNotification.getPreview(),teamsNotification.getContent(), userId);

        sendPostRequest(endPoint, payload);
    }

    private static String sendProactiveMessage(String preview, String content,String userId) throws IOException {
        System.out.println("preview: " + preview);
        System.out.println("content: " + content);
        return "";
    }

    protected String getAuthHeader() {
        return "Bearer " + accessToken;
    }

    public static TeamsClient create() throws DotenvException, IOException, URISyntaxException {

        Dotenv dotenv = Dotenv.configure().directory(Config.getProjectRoot()).load();

        final String tenantId = dotenv.get("TEAMS_TENANT_ID");
        final String clientId = dotenv.get("TEAMS_CLIENT_ID");
        final String clientSecret = dotenv.get("TEAMS_CLIENT_SECRET");
        final String token = getAccessToken(tenantId, clientId, clientSecret);
        return new TeamsClient(token);
    }

    private static String getAccessToken(String tenantId, String clientId, String clientSecret)
            throws IOException, DotenvException, URISyntaxException {

        String authority = "https://login.microsoftonline.com/" + tenantId;
        String scope = "https://graph.microsoft.com/.default";
        String endpoint = authority + "/oauth2/v2.0/token";

        Map<String, String> formData = new HashMap<>();
        formData.put("client_id", clientId);
        formData.put("client_secret", clientSecret);
        formData.put("scope", scope);
        formData.put("grant_type", "client_credentials");

        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getFormDataString(formData));
        writer.flush();

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            BufferedReader error = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String response = error.lines().collect(Collectors.joining());
            throw new IOException("Failed to retrieve token " + response);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = in.lines().collect(Collectors.joining());

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonResponse = mapper.readTree(response);
        String accessToken = jsonResponse.get("access_token").asText();

        return accessToken;
    }


    private static String getFormDataString(Map<String, String> formData) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
