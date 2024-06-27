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
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.items.TeamsNotification;
import com.tool.Config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class TeamsClient extends NetworkServiceClient {

    private final HashMap<String, String> emailMap;
    private String clientSecret;
    private String clientId;
    private String tenantId;
    private String installationId;

    private TeamsClient(String tenantId, String clientId, String clientSecret, String installationId) {
        this.emailMap = new HashMap<>();
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.installationId = installationId;
    }

    public void createNotification(TeamsNotification teamsNotification)
            throws IOException, DotenvException, URISyntaxException {

        String userId = getIdByEmail(teamsNotification.getEmail());

        String endPoint = "https://graph.microsoft.com/v1.0/users/" + userId + "/teamwork/sendActivityNotification";

        String payload = getBody(teamsNotification.getPreview(), teamsNotification.getContent(), userId);

        sendPostRequest(endPoint, payload);
    }

    private String getBody(String preview, String content, String userId) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

            ObjectNode payload = objectMapper.createObjectNode();
            
            ObjectNode topic = payload.putObject("topic");
            topic.put("source", "entityUrl");
            topic.put("value", "https://graph.microsoft.com/v1.0/users/" + userId + "/teamwork/installedApps/" + installationId);

            payload.put("activityType", "taskCreated");
            
            ObjectNode previewText = payload.putObject("previewText");
            previewText.put("content", preview);
            
            ObjectNode templateParameter = objectMapper.createObjectNode();
            templateParameter.put("name", "regression-blame");
            templateParameter.put("value", content);

            payload.putArray("templateParameters").add(templateParameter);



        return objectMapper.writeValueAsString(payload);
    }

    protected String getAuthHeader() throws DotenvException, IOException, URISyntaxException {
        return "Bearer " + getAccessToken();
    }

    public static TeamsClient create() throws DotenvException, IOException, URISyntaxException {
        Dotenv dotenv = Dotenv.configure().directory(Config.getProjectRoot()).load();
        final String tenantId = dotenv.get("TEAMS_TENANT_ID");
        final String clientId = dotenv.get("TEAMS_CLIENT_ID");
        final String clientSecret = dotenv.get("TEAMS_CLIENT_SECRET");
        final String installationId = dotenv.get("TEAMS_APP_ID");
        return new TeamsClient(tenantId, clientId, clientSecret, installationId);
    }

    private String getIdByEmail(String email) throws IOException, DotenvException, URISyntaxException {
        if (email == "LAST PHASE")
            return null;

        if (!emailMap.containsKey(email)) {
            String endpoint = "https://graph.microsoft.com/v1.0/users?$filter=mail eq '" + email + "'";
            String response = sendGetRequest(endpoint);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response);
            String id = jsonResponse.get("value").get(0).get("id").asText();
            emailMap.put(email, id);
        }
        return emailMap.get(email);
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

    private String getAccessToken() throws IOException, DotenvException, URISyntaxException {

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

}
