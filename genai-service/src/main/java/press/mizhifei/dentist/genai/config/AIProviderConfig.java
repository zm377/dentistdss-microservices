package press.mizhifei.dentist.genai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
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
    private String defaultProvider = "openai";

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
        private boolean enabled = false;

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
        private String defaultModel = "gemini-2.5-pro-20250506";

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
     * Creates the primary ChatClient bean with provider switching capability
     */
    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder openAiChatClientBuilder,
                               ChatClient.Builder vertexAiChatClientBuilder) {
        
        log.info("Configuring AI providers - Default: {}, OpenAI enabled: {}, VertexAI enabled: {}", 
                defaultProvider, openai.isEnabled(), vertexai.isEnabled());

        // For now, return the default provider's client
        // The ProviderSwitchingChatClient will handle the actual switching logic
        if ("vertexai".equalsIgnoreCase(defaultProvider) && vertexai.isEnabled()) {
            log.info("Using Vertex AI as primary provider");
            return vertexAiChatClientBuilder.build();
        } else if (openai.isEnabled()) {
            log.info("Using OpenAI as primary provider");
            return openAiChatClientBuilder.build();
        } else {
            throw new IllegalStateException("No AI provider is enabled or configured properly");
        }
    }

    /**
     * Creates OpenAI ChatClient bean
     */
    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(ChatClient.Builder builder) {
        if (!openai.isEnabled()) {
            log.warn("OpenAI provider is disabled");
            return null;
        }

        return builder.build();
    }

    /**
     * Creates Vertex AI ChatClient bean (if available)
     */
    @Bean("vertexAiChatClient")
    public ChatClient vertexAiChatClient(ChatClient.Builder builder) {
        if (!vertexai.isEnabled()) {
            log.warn("Vertex AI provider is disabled");
            return null;
        }

        try {
            return builder.build();
        } catch (Exception e) {
            log.error("Failed to configure Vertex AI ChatClient: {}", e.getMessage());
            return null;
        }
    }
}
