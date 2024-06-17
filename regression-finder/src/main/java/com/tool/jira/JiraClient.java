package com.tool.jira;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;

import com.items.interfaces.JiraTicket;

public class JiraClient {
    private final String jiraUrl;
    private final String username;
    private final String apiToken;
    private final HashMap<String, String> emailMap;
    private static final int ID_OFFSET = 13;
    private static final int ID_LENGTH = 43;

    public JiraClient(String jiraUrl, String username, String apiToken) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.emailMap = new HashMap<>();
    }

    public static JiraClient createAPI(String jiraUrl, String username, String apiToken) {
        return new JiraClient(jiraUrl, username, apiToken);
    }

    public void createIssue(String projectKey, Long issueTypeId, JiraTicket jiraTicket)
            throws IOException {

        String endpoint = jiraUrl + "/rest/api/3/issue";
        String requestBody = getRequestBody(projectKey, issueTypeId, jiraTicket);
        sendPostRequest(endpoint, requestBody);
    }

    public void close() {
        System.out.println("Closed Jira Client");
    }

    private String getRequestBody(String projectKey, Long issueTypeId, JiraTicket jiraTicket) throws IOException {

        String assigneeId = getIdByEmail(jiraTicket.getEmail());

        String requestBody = "{ \"fields\": { \"project\": { \"key\": \"" + projectKey + "\" }, " +
                "\"summary\": \"" + jiraTicket.getSummary() + "\", " +
                "\"assignee\": { \"id\": \"" + assigneeId + "\" }, " +
                "\"description\": { " +
                "\"type\": \"doc\", " +
                "\"version\": 1, " +
                "\"content\": [ { " +
                "\"type\": \"paragraph\", " +
                "\"content\": [ { " +
                "\"text\": \"" + jiraTicket.getDescription() + "\", " +
                "\"type\": \"text\" " +
                "} ] " +
                "} ] " +
                "}, " +
                "\"issuetype\": { \"id\": \"" + issueTypeId + "\" } } }";
        return requestBody;
    }

    public String getIdByEmail(String email) throws IOException {

        if (!emailMap.containsKey(email)) {
            String endpoint = jiraUrl + "/rest/api/3/user/search?query=" + URLEncoder.encode(email, "UTF-8");
            String response = sendGetRequest(endpoint);
            int idStart = response.indexOf("\"accountId\"", 0) + ID_OFFSET;
            String assigneeId = response.substring(idStart, idStart + ID_LENGTH);
            emailMap.put(email, assigneeId);
        }

        return emailMap.get(email);
    }

    private String sendGetRequest(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getAuthHeader());

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    private void sendPostRequest(String endpoint, String requestBody) throws IOException {
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

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
    }

    private String getAuthHeader() {
        String auth = username + ":" + apiToken;
        byte[] authBytes = auth.getBytes();
        String encodedAuth = Base64.getEncoder().encodeToString(authBytes);
        return "Basic " + encodedAuth;
    }
}
