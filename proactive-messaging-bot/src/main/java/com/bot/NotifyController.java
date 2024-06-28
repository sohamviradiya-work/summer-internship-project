
package com.bot;

import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.ConversationReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class NotifyController {

    private final BotFrameworkHttpAdapter adapter;

    private ConversationReferences conversationReferences;
    private String appId;
    private final HashMap<String,String> emailToidMap;

    @Autowired
    public NotifyController(
            BotFrameworkHttpAdapter withAdapter,
            Configuration withConfiguration,
            ConversationReferences withReferences) {
        adapter = withAdapter;
        conversationReferences = withReferences;
        appId = withConfiguration.getProperty("MicrosoftAppId");
        emailToidMap = new HashMap<>();
    }

    public static class NotificationRequest {
        private Notification notification;

        public Notification getNotification() {
            return notification;
        }

        public void setNotification(Notification notification) {
            this.notification = notification;
        }

        public static class Notification {
            private String description;

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }
    }

    @GetMapping("/api/change")
    public ResponseEntity<Object> changeMail(@RequestParam String email,@RequestParam String id) {
        
        emailToidMap.values().removeIf(value -> value.equals(id));
        emailToidMap.put(email, id);

        return new ResponseEntity<>(
                "<html><body><h1>Changed " + id + " to point to " + email,
                HttpStatus.ACCEPTED);
    }

    @PostMapping("/api/notify")
    public ResponseEntity<Object> proactiveMessage(@RequestParam String email,@RequestBody NotificationRequest request) {
        
        String id = emailToidMap.get(email);

        if(id==null) new ResponseEntity<>(
            "<html><body><h1>" + id + " not found.</h1></body></html>",
            HttpStatus.ACCEPTED);

        for (ConversationReference reference : conversationReferences.values()) {
            if (reference.getUser().getId().equals(id))
                adapter.continueConversation(appId, reference,
                        turnContext -> turnContext.sendActivity(request.getNotification().getDescription())
                                .thenApply(resourceResponse -> null));
        }

        return new ResponseEntity<>(
                "<html><body><h1>Proactive messages have been sent to " + id + ".</h1></body></html>",
                HttpStatus.CREATED);
    }

}
