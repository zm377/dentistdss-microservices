package com.dentistdss.genai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for multiple AI providers with dynamic switching capability
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "genai.providers")
@Data
public class AIProviderConfig {

    /**
     * Vertex AI specific configuration
     */
    private VertexAIConfig vertexai = new VertexAIConfig();



    @Data
    public static class VertexAIConfig {
        /**
         * Whether Vertex AI provider is enabled
         */
        private boolean enabled = true;

        /**
         * Google Cloud Project ID
         */
        private String projectId;

        /**
         * Google Cloud Location/Region
         */
        private String location = "us-central1";

        /**
         * Default model to use
         */
        private String defaultModel = "gemini-2.5-pro-preview-05-06";

        /**
         * Weight for load balancing (higher = more requests)
         */
        private int weight = 50;

        /**
         * Maximum tokens per request
         */
        private int maxTokens = 4096;

        /**
         * Temperature for response randomness
         */
        private double temperature = 0.7;
    }

    /**
     * Sets Vertex AI as the primary ChatModel
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(@Qualifier("vertexAiGeminiChat") ChatModel vertexAiChatModel) {
        log.info("Configuring Vertex AI Gemini as primary ChatModel - enabled: {}", vertexai.enabled);

        if (vertexai.enabled) {
            log.info("Setting Vertex AI Gemini as primary ChatModel");
            return vertexAiChatModel;
        }

        throw new IllegalStateException("Vertex AI provider is not enabled or configured properly");
    }

    /**
     * Creates the primary ChatClient bean using Vertex AI
     */
    @Bean("primaryChatClient")
    @Primary
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        log.info("Configuring primary ChatClient using Vertex AI ChatModel");
        return chatClientBuilder.build();
    }

    /**
     * Creates Vertex AI ChatClient bean
     */
    @Bean("vertexAiChatClient")
    public ChatClient vertexAiChatClient(@Qualifier("vertexAiGeminiChat") ChatModel vertexAiChatModel) {
        if (!vertexai.enabled) {
            log.warn("Vertex AI provider is disabled");
            return null;
        }
        try {
            return ChatClient.builder(vertexAiChatModel).build();
        } catch (Exception e) {
            log.error("Failed to configure Vertex AI ChatClient: {}", e.getMessage());
            return null;
        }
    }
}
