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

import com.items.interfaces.TeamsItem;
import com.tool.clients.TeamsClient;
import com.tool.writers.interfaces.ItemWriter;

import io.github.cdimascio.dotenv.DotenvException;

public class TeamsNotificationWriter<T extends TeamsItem> implements ItemWriter<T> {
    
    private TeamsClient client;
    private ExecutorService executorService;
    private List<Future<?>> futures;

    public TeamsNotificationWriter(TeamsClient client) {
        this.client = client;
        this.executorService = Executors.newCachedThreadPool();
        this.futures = new ArrayList<>();
    }

    @Override
    public void write(T item) throws IOException {
        futures.add(executorService.submit(() -> {
            try {
                writeNotification(item);
            } catch (IOException | URISyntaxException e) {
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

    protected void writeNotification(T item) throws IOException, URISyntaxException {
        this.client.createNotification(item.toTeamsNotification());
    }

    public static <T extends TeamsItem> TeamsNotificationWriter<T> create() throws IOException, DotenvException, URISyntaxException {

        TeamsClient teamsClient = TeamsClient.create();

        return new TeamsNotificationWriter<>(teamsClient);
    }
}
