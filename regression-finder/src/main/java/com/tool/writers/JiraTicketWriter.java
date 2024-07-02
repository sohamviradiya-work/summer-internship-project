package com.tool.writers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.items.JiraTicket;
import com.items.interfaces.JiraItem;
import com.tool.clients.JiraClient;
import com.tool.writers.interfaces.ItemWriter;

import io.github.cdimascio.dotenv.DotenvException;

public class JiraTicketWriter<T extends JiraItem> implements ItemWriter<T> {

    private JiraClient client;
    private HashMap<String, List<String>> issueMap;
    List<Future<?>> futures;
    private ExecutorService executorService;

    public JiraTicketWriter(JiraClient client) {
        this.client = client;
        this.issueMap = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.futures = new ArrayList<>();
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
        if (executorService.isShutdown())
            return;
        submitIssues();
        executorService.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void submitIssues() {
        for (String email : issueMap.keySet()) {
            submitIssue(email);
        }
    }

    private void submitIssue(String email) {
        futures.add(executorService.submit(() -> {
            StringBuilder descriptionBuilder = new StringBuilder();
            for (String lines : issueMap.get(email))
                descriptionBuilder.append(lines + "\n\n");
            try {
                this.client.createIssue(email, descriptionBuilder.toString());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }));
    }

    protected void writeIssue(T item) {
        JiraTicket jiraTicket = item.toJiraTicket();
        issueMap.computeIfAbsent(jiraTicket.getEmail(), k -> new ArrayList<>()).add(jiraTicket.getDescription());
    }

    public static <T extends JiraItem> JiraTicketWriter<T> create()
            throws IOException, DotenvException, URISyntaxException {

        JiraClient jiraClient = JiraClient.create();

        return new JiraTicketWriter<>(jiraClient);
    }
}
