package press.mizhifei.dentist.genai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.genai.config.AIProviderConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing AI provider switching and failover
 * Handles dynamic switching between OpenAI and Vertex AI providers
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIProviderService {

    private final AIProviderConfig config;
    
    @Qualifier("openAiChatClient")
    private final ChatClient openAiChatClient;
    
    @Qualifier("vertexAiChatClient")
    private final ChatClient vertexAiChatClient;

    // Load balancing counter for round-robin
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /**
     * Executes a chat request with provider switching and failover
     * @param prompt the chat prompt
     * @return chat response
     */
    public Mono<String> chat(Prompt prompt) {
        String selectedProvider = selectProvider();
        
        return executeWithProvider(prompt, selectedProvider, false)
                .onErrorResume(error -> {
                    if (config.getSwitching().isEnableFailover()) {
                        log.warn("Primary provider {} failed, attempting failover: {}", 
                                selectedProvider, error.getMessage());
                        String fallbackProvider = getFallbackProvider(selectedProvider);
                        return executeWithProvider(prompt, fallbackProvider, true);
                    } else {
                        return Mono.error(error);
                    }
                });
    }

    /**
     * Executes a streaming chat request with provider switching and failover
     * @param prompt the chat prompt
     * @return streaming chat response
     */
    public Flux<String> streamChat(Prompt prompt) {
        String selectedProvider = selectProvider();
        
        return executeStreamWithProvider(prompt, selectedProvider, false)
                .onErrorResume(error -> {
                    if (config.getSwitching().isEnableFailover()) {
                        log.warn("Primary provider {} failed for streaming, attempting failover: {}", 
                                selectedProvider, error.getMessage());
                        String fallbackProvider = getFallbackProvider(selectedProvider);
                        return executeStreamWithProvider(prompt, fallbackProvider, true);
                    } else {
                        return Flux.error(error);
                    }
                });
    }

    /**
     * Selects the appropriate provider based on configuration
     */
    private String selectProvider() {
        if (config.getSwitching().isEnableLoadBalancing()) {
            return selectProviderWithLoadBalancing();
        } else {
            return config.getDefaultProvider();
        }
    }

    /**
     * Selects provider using load balancing strategy
     */
    private String selectProviderWithLoadBalancing() {
        String strategy = config.getSwitching().getLoadBalancingStrategy();
        
        switch (strategy.toLowerCase()) {
            case "round_robin":
                return selectRoundRobin();
            case "random":
                return selectRandom();
            case "weighted":
                return selectWeighted();
            default:
                log.warn("Unknown load balancing strategy: {}, falling back to default", strategy);
                return config.getDefaultProvider();
        }
    }

    /**
     * Round-robin provider selection
     */
    private String selectRoundRobin() {
        int count = requestCounter.getAndIncrement();
        if (count % 2 == 0 && config.getOpenai().isEnabled()) {
            return "openai";
        } else if (config.getVertexai().isEnabled()) {
            return "vertexai";
        } else {
            return "openai"; // Fallback
        }
    }

    /**
     * Random provider selection
     */
    private String selectRandom() {
        return Math.random() < 0.5 ? "openai" : "vertexai";
    }

    /**
     * Weighted provider selection
     */
    private String selectWeighted() {
        int openAiWeight = config.getOpenai().getWeight();
        int vertexAiWeight = config.getVertexai().getWeight();
        int totalWeight = openAiWeight + vertexAiWeight;
        
        if (totalWeight == 0) {
            return config.getDefaultProvider();
        }
        
        double random = Math.random() * totalWeight;
        return random < openAiWeight ? "openai" : "vertexai";
    }

    /**
     * Gets the fallback provider for the given primary provider
     */
    private String getFallbackProvider(String primaryProvider) {
        if ("openai".equals(primaryProvider) && config.getVertexai().isEnabled()) {
            return "vertexai";
        } else if ("vertexai".equals(primaryProvider) && config.getOpenai().isEnabled()) {
            return "openai";
        } else {
            return primaryProvider; // No fallback available
        }
    }

    /**
     * Executes chat request with specific provider
     */
    private Mono<String> executeWithProvider(Prompt prompt, String provider, boolean isFailover) {
        ChatClient client = getClientForProvider(provider);
        if (client == null) {
            return Mono.error(new IllegalStateException("Provider " + provider + " is not available"));
        }

        log.debug("Executing chat with provider: {} (failover: {})", provider, isFailover);
        
        return Mono.fromSupplier(() -> client.prompt(prompt).call().content())
                .timeout(Duration.ofMillis(config.getSwitching().getTimeoutMs()))
                .doOnSuccess(response -> log.debug("Successfully executed chat with provider: {}", provider))
                .doOnError(error -> log.error("Failed to execute chat with provider {}: {}", 
                        provider, error.getMessage()));
    }

    /**
     * Executes streaming chat request with specific provider
     */
    private Flux<String> executeStreamWithProvider(Prompt prompt, String provider, boolean isFailover) {
        ChatClient client = getClientForProvider(provider);
        if (client == null) {
            return Flux.error(new IllegalStateException("Provider " + provider + " is not available"));
        }

        log.debug("Executing streaming chat with provider: {} (failover: {})", provider, isFailover);
        
        return client.prompt(prompt).stream().content()
                .timeout(Duration.ofMillis(config.getSwitching().getTimeoutMs()))
                .doOnComplete(() -> log.debug("Successfully completed streaming chat with provider: {}", provider))
                .doOnError(error -> log.error("Failed to execute streaming chat with provider {}: {}", 
                        provider, error.getMessage()));
    }

    /**
     * Gets the ChatClient for the specified provider
     */
    private ChatClient getClientForProvider(String provider) {
        switch (provider.toLowerCase()) {
            case "openai":
                return config.getOpenai().isEnabled() ? openAiChatClient : null;
            case "vertexai":
                return config.getVertexai().isEnabled() ? vertexAiChatClient : null;
            default:
                log.warn("Unknown provider: {}", provider);
                return null;
        }
    }

    /**
     * Gets the current provider status
     */
    public ProviderStatus getProviderStatus() {
        return ProviderStatus.builder()
                .defaultProvider(config.getDefaultProvider())
                .openAiEnabled(config.getOpenai().isEnabled())
                .vertexAiEnabled(config.getVertexai().isEnabled())
                .failoverEnabled(config.getSwitching().isEnableFailover())
                .loadBalancingEnabled(config.getSwitching().isEnableLoadBalancing())
                .totalRequests(requestCounter.get())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class ProviderStatus {
        private String defaultProvider;
        private boolean openAiEnabled;
        private boolean vertexAiEnabled;
        private boolean failoverEnabled;
        private boolean loadBalancingEnabled;
        private int totalRequests;
    }
}
