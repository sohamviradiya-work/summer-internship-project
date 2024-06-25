package com.tool.writers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.items.interfaces.JiraItem;
import com.tool.clients.JiraClient;
import com.tool.writers.interfaces.ItemWriter;

import io.github.cdimascio.dotenv.DotenvException;

public class JiraTicketWriter<T extends JiraItem> implements ItemWriter<T> {

    private JiraClient client;
    private ExecutorService executorService;
    private List<Future<?>> futures;

    public JiraTicketWriter(JiraClient client) {
        this.client = client;
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
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    protected void writeIssue(T item) throws IOException {
        this.client.createIssue(item.toJiraTicket());
    }

    public static <T extends JiraItem> JiraTicketWriter<T> create() throws IOException, DotenvException, URISyntaxException {

        JiraClient jiraClient = JiraClient.create();

        return new JiraTicketWriter<>(jiraClient);
    }
}
