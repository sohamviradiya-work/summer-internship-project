// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.bot;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication

@Import({BotController.class})

public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public Bot getBot(ConversationReferences conversationReferences) {
        return new ProactiveBot(conversationReferences);
    }

    @Bean
    public ConversationReferences getConversationReferences() {
        return new ConversationReferences();
    }

    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new AdapterWithErrorHandler(configuration);
    }
}
