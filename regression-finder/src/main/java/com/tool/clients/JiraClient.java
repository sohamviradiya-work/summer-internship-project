package com.tool.clients;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;

import com.items.JiraTicket;
import com.tool.Config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class JiraClient extends NetworkServiceClient {
    private final String jiraUrl;
    private final String username;
    private final String apiToken;
    private final HashMap<String, String> emailMap;
    private final long issueTypeId;
    private final String projectKey;
    private static final int ID_OFFSET = 13;
    private static final int ID_LENGTH = 43;

    public JiraClient(String jiraUrl, String username, String apiToken, String issueTypeId, String projectKey) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.issueTypeId = Long.parseLong(issueTypeId);
        this.projectKey = projectKey;
        this.emailMap = new HashMap<>();
    }

    public void createIssue(JiraTicket jiraTicket)
            throws IOException {
        String endpoint = jiraUrl + "/rest/api/3/issue";
        String requestBody = getRequestBody(jiraTicket);
        sendPostRequest(endpoint, requestBody);
    }

    public void close() {
        System.out.println("Closed Jira Client");
    }

    private String getRequestBody(JiraTicket jiraTicket) throws IOException {

        String assigneeId = getIdByEmail(jiraTicket.getEmail());

        String assigneeEntry = "";

        if (assigneeId != null)
            assigneeEntry = "\"assignee\": { \"id\": \"" + assigneeId + "\" }, ";

        String requestBody = "{ \"fields\": { \"project\": { \"key\": \"" + projectKey + "\" }, " +
                "\"summary\": \"" + jiraTicket.getSummary() + "\", " +
                assigneeEntry +
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

    private String getIdByEmail(String email) throws IOException {

        if (email == "LAST PHASE")
            return null;

        if (!emailMap.containsKey(email)) {
            String endpoint = jiraUrl + "/rest/api/3/user/search?query=" + URLEncoder.encode(email, "UTF-8");
            String response = sendGetRequest(endpoint);
            int idStart = response.indexOf("\"accountId\"", 0) + ID_OFFSET;
            String assigneeId = response.substring(idStart, idStart + ID_LENGTH);
            emailMap.put(email, assigneeId);
        }

        return emailMap.get(email);
    }

    protected String getAuthHeader() {
        String auth = username + ":" + apiToken;
        byte[] authBytes = auth.getBytes();
        String encodedAuth = Base64.getEncoder().encodeToString(authBytes);
        return "Basic " + encodedAuth;
    }

    public static JiraClient createClient() throws DotenvException, URISyntaxException {

        Dotenv dotenv = Dotenv.configure().directory(Config.getProjectRoot()).load();

        final String jiraUrl = dotenv.get("JIRA_SERVER");
        final String email = dotenv.get("JIRA_MAIL");
        final String token = dotenv.get("JIRA_TOKEN");
        final String issueTypeId = dotenv.get("JIRA_ISSUE_TYPE");
        final String projectKey = dotenv.get("JIRA_PROJECT_KEY");
        JiraClient jiraClient = new JiraClient(jiraUrl, email, token, issueTypeId, projectKey);
        return jiraClient;
    }
}
