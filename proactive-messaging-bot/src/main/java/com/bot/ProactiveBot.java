// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProactiveBot extends ActivityHandler {

    @Value("${server.port:3978}")
    private int port;

    private ConversationReferences conversationReferences;

    public ProactiveBot(ConversationReferences withReferences) {
        conversationReferences = withReferences;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());

        String messageText = turnContext.getActivity().getText();

        if (messageText.startsWith("Add mail ")) {
            String email = extractEmail(messageText);
            if (email != null) {
                String userId = turnContext.getActivity().getFrom().getId();

                CompletableFuture<Void> completableFuture = sendRequestToEndpoint(email, userId);

                try {
                    completableFuture.get();
                    return turnContext.sendActivity(MessageFactory.text("Email added successfully: " + email))
                            .thenApply(sendResult -> null);
                } catch (InterruptedException | ExecutionException e) {
                    return turnContext.sendActivity(MessageFactory.text("Failed to add email. Please try again later."))
                            .thenApply(sendResult -> null);
                }

            } else {
                return turnContext.sendActivity(MessageFactory.text("Invalid email format: " + messageText))
                        .thenApply(sendResult -> null);
            }
        } else {
            return turnContext.sendActivity(MessageFactory.text("No valid command detected."))
                    .thenApply(sendResult -> null);
        }
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext) {

        membersAdded.forEach(member -> System.out.println(member.getId()));

        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId()))
                .map(
                        channel -> turnContext
                                .sendActivity(MessageFactory.text(String.format(
                                        "Please send your mail in format: Add mail: <abc@mail.com>", port))))
                .collect(CompletableFutures.toFutureList())
                .thenApply(resourceResponses -> null);
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        return super.onConversationUpdateActivity(turnContext);
    }

    private void addConversationReference(Activity activity) {
        ConversationReference conversationReference = activity.getConversationReference();
        conversationReferences.put(conversationReference.getUser().getId(), conversationReference);
    }

    private String extractEmail(String messageText) {
        Pattern emailPattern = Pattern.compile("Add mail (.+@.+\\..+)");
        Matcher matcher = emailPattern.matcher(messageText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private CompletableFuture<Void> sendRequestToEndpoint(String email, String userId) {
        String apiUrl = "https://regressionfinderbot-regressionfinder.azuremicroservices.io/api/change";

        String queryParams = String.format("id=%s&email=%s", userId, email);

        try {
            URL url = new URL(apiUrl + "?" + queryParams);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            int statusCode = conn.getResponseCode();
            if (statusCode == 200) {
                System.out.println("Request successful");
            } else {
                System.out.println("Request failed with status code: " + statusCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }
}
