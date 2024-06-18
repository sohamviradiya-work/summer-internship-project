package com.tool.writers;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.items.interfaces.JiraItem;
import com.tool.jira.JiraClient;
import com.tool.writers.interfaces.ItemWriter;

import io.github.cdimascio.dotenv.Dotenv;

public class JiraTicketWriter<T extends JiraItem> implements ItemWriter<T> {

    private static final int TIMEOUT_MINUTES = 10;
    private JiraClient client;
    private String issueTypeId;
    private String projectKey;
    private ExecutorService executorService;

    public JiraTicketWriter(JiraClient client, String issueTypeId, String projectKey) {
        this.client = client;
        this.issueTypeId = issueTypeId;
        this.projectKey = projectKey;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void write(T item) throws IOException {
        writeIssue(item);
    }

    @Override
    public void writeAll(Collection<T> items) throws IOException {
        for (T item : items)
            write(item);
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
    }

    protected void writeIssue(T item) throws IOException {
        this.client.createIssue(projectKey, Long.parseLong(issueTypeId), item.toJiraTicket());
    }

    public static <T extends JiraItem> JiraTicketWriter<T> create() throws IOException {

        Dotenv dotenv = Dotenv.configure().directory("../").load();

        final String jiraUrl = dotenv.get("JIRA_SERVER");
        final String email = dotenv.get("JIRA_MAIL");
        final String token = dotenv.get("JIRA_TOKEN");
        final String issueTypeId = dotenv.get("JIRA_ISSUE_TYPE");
        final String projectKey = dotenv.get("JIRA_PROJECT_KEY");

        JiraClient jiraClient = new JiraClient(jiraUrl, email, token);

        return new JiraTicketWriter<>(jiraClient, issueTypeId, projectKey);
    }

}
