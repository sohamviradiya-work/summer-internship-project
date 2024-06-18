package com.tool.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.items.interfaces.JiraItem;
import com.tool.jira.JiraClient;
import com.tool.writers.interfaces.ItemWriter;

import io.github.cdimascio.dotenv.Dotenv;

public class JiraTicketWriter<T extends JiraItem> implements ItemWriter<T> {

    private JiraClient client;
    private String issueTypeId;
    private String projectKey;
    private ExecutorService executorService;
    private List<Future<?>> futures;

    public JiraTicketWriter(JiraClient client, String issueTypeId, String projectKey) {
        this.client = client;
        this.issueTypeId = issueTypeId;
        this.projectKey = projectKey;
        this.executorService = Executors.newCachedThreadPool();
        this.futures = new ArrayList<>();
    }

    @Override
    public void write(T item) throws IOException {
        futures.add(executorService.submit(() -> {
            try {
                writeIssue(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void writeAll(Collection<T> items) throws IOException {
        for (T item : items)
            write(item);
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        }
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
