package com.tool.clients;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final String issueTransitionId;

    public JiraClient(String jiraUrl, String username, String apiToken, String issueTypeId, String issueTransitionId,
            String projectKey) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.issueTypeId = Long.parseLong(issueTypeId);
        this.issueTransitionId = issueTransitionId;
        this.projectKey = projectKey;
        this.emailMap = new HashMap<>();
    }

    public void createIssue(String email, String description)
            throws IOException, URISyntaxException {
        String endpoint = jiraUrl + "/rest/api/3/issue";
        String requestBody = initiateTicketBody(email, description);
        sendPostRequest(endpoint, requestBody);
    }

    private String initiateTicketBody(String email, String description)
            throws IOException, URISyntaxException {

        String assigneeId = getIdByEmail(email);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        ObjectNode fieldsNode = mapper.createObjectNode();
        ObjectNode projectNode = fieldsNode.putObject("project");
        projectNode.put("key", projectKey);

        fieldsNode.put("summary", "Your commits caused some regressions");

        if (assigneeId != null) {
            ObjectNode assigneeNode = fieldsNode.putObject("assignee");
            assigneeNode.put("id", assigneeId);
        }

        ObjectNode descriptionNode = fieldsNode.putObject("description");
        descriptionNode.put("type", "doc");
        descriptionNode.put("version", 1);

        ObjectNode descriptionContentNode = mapper.createObjectNode();
        descriptionContentNode.put("text", description);
        descriptionContentNode.put("type", "text");

        ObjectNode paragraphNode = mapper.createObjectNode();
        paragraphNode.put("type", "paragraph");
        paragraphNode.putArray("content").add(descriptionContentNode);

        descriptionNode.putArray("content").add(paragraphNode);

        ObjectNode issuetypeNode = fieldsNode.putObject("issuetype");
        issuetypeNode.put("id", issueTypeId);

        ObjectNode transitionNode = mapper.createObjectNode();
        transitionNode.put("id", issueTransitionId);

        ObjectNode requestBodyNode = mapper.createObjectNode();
        requestBodyNode.set("fields", fieldsNode);
        requestBodyNode.set("transition", transitionNode);

        String requestBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBodyNode);

        return requestBody;
    }

    private String getIdByEmail(String email) throws IOException, URISyntaxException {

        if (email == "LAST PHASE")
            return null;

        if (!emailMap.containsKey(email)) {
            String endpoint = jiraUrl + "/rest/api/3/user/search?query=" + URLEncoder.encode(email, "UTF-8");
            String response = sendGetRequest(endpoint);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response);
            String assigneeId = jsonResponse.get(0).get("accountId").asText();
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

    public static JiraClient create() throws DotenvException, URISyntaxException {

        Dotenv dotenv = Dotenv.configure().directory(Config.getProjectRoot()).load();

        final String jiraUrl = dotenv.get("JIRA_SERVER");
        final String email = dotenv.get("JIRA_MAIL");
        final String token = dotenv.get("JIRA_TOKEN");
        final String issueTypeId = dotenv.get("JIRA_ISSUE_TYPE");
        final String issueTransitionId = dotenv.get("JIRA_ISSUE_TRANSITION");
        final String projectKey = dotenv.get("JIRA_PROJECT_KEY");
        JiraClient jiraClient = new JiraClient(jiraUrl, email, token, issueTypeId, issueTransitionId, projectKey);
        return jiraClient;
    }
}
