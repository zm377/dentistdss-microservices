package press.mizhifei.dentist.genai.config;

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
     * Default provider to use (openai or vertexai)
     */
    private String defaultProvider = "vertexai";

    /**
     * Provider switching configuration
     */
    private ProviderSwitching switching = new ProviderSwitching();

    /**
     * OpenAI specific configuration
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * Vertex AI specific configuration
     */
    private VertexAIConfig vertexai = new VertexAIConfig();

    @Data
    public static class ProviderSwitching {
        /**
         * Enable automatic failover to secondary provider
         */
        private boolean enableFailover = true;

        /**
         * Maximum retry attempts before switching provider
         */
        private int maxRetries = 3;

        /**
         * Timeout in milliseconds before considering a request failed
         */
        private long timeoutMs = 30000;

        /**
         * Enable load balancing between providers
         */
        private boolean enableLoadBalancing = false;

        /**
         * Load balancing strategy (round_robin, random, weighted)
         */
        private String loadBalancingStrategy = "round_robin";
    }

    @Data
    public static class OpenAIConfig {
        /**
         * Whether OpenAI provider is enabled
         */
        private boolean enabled = true;

        /**
         * Default model to use
         */
        private String defaultModel = "gpt-4.1-nano-2025-04-14";

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
        private String defaultModel = "gemini-2.5-pro-preview-06-05";

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
     * Sets Vertex AI Gemini as the primary ChatModel to resolve auto-configuration conflicts
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(@Qualifier("vertexAiGeminiChat") ChatModel vertexAiChatModel,
                                     @Qualifier("openAiChatModel") ChatModel openAiChatModel) {

        log.info("Configuring primary ChatModel - Default provider: {}, VertexAI enabled: {}, OpenAI enabled: {}",
                defaultProvider, vertexai.isEnabled(), openai.isEnabled());

        // Set Vertex AI Gemini as primary as requested
        if (vertexai.isEnabled()) {
            log.info("Setting Vertex AI Gemini as primary ChatModel");
            return vertexAiChatModel;
        } else if (openai.isEnabled()) {
            log.info("Falling back to OpenAI as primary ChatModel (Vertex AI disabled)");
            return openAiChatModel;
        } else {
            throw new IllegalStateException("No AI provider is enabled or configured properly");
        }
    }

    /**
     * Creates the primary ChatClient bean with provider switching capability
     */
    @Bean("primaryChatClient")
    @Primary
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {

        log.info("Configuring primary ChatClient using primary ChatModel");

        // The ChatClient.Builder will automatically use the @Primary ChatModel
        return chatClientBuilder.build();
    }

    /**
     * Creates OpenAI ChatClient bean
     */
    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        if (!openai.isEnabled()) {
            log.warn("OpenAI provider is disabled");
            return null;
        }

        return ChatClient.builder(openAiChatModel).build();
    }

    /**
     * Creates Vertex AI ChatClient bean (if available)
     */
    @Bean("vertexAiChatClient")
    public ChatClient vertexAiChatClient(@Qualifier("vertexAiGeminiChat") ChatModel vertexAiChatModel) {
        if (!vertexai.isEnabled()) {
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
