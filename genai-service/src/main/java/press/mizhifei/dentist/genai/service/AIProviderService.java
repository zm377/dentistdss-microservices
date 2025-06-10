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



/**
 * Service for managing Vertex AI provider
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

    @Qualifier("vertexAiChatClient")
    private final ChatClient vertexAiChatClient;

    /**
     * Executes a chat request using Vertex AI
     * @param prompt the chat prompt
     * @return chat response
     */
    public Mono<String> chat(Prompt prompt) {
        if (!config.getVertexai().isEnabled() || vertexAiChatClient == null) {
            return Mono.error(new IllegalStateException("Vertex AI provider is not enabled or configured"));
        }

        return Mono.fromSupplier(() -> vertexAiChatClient.prompt(prompt).call().content())
                .doOnSuccess(response -> log.debug("Vertex AI chat completed successfully"))
                .doOnError(error -> log.error("Vertex AI chat failed: {}", error.getMessage()));
    }

    /**
     * Executes a streaming chat request using Vertex AI
     * @param prompt the chat prompt
     * @return streaming chat response
     */
    public Flux<String> streamChat(Prompt prompt) {
        if (!config.getVertexai().isEnabled() || vertexAiChatClient == null) {
            return Flux.error(new IllegalStateException("Vertex AI provider is not enabled or configured"));
        }

        return vertexAiChatClient.prompt(prompt).stream().content()
                .doOnNext(chunk -> log.trace("Vertex AI stream chunk received"))
                .doOnComplete(() -> log.debug("Vertex AI stream completed"))
                .doOnError(error -> log.error("Vertex AI stream failed: {}", error.getMessage()));
    }



    /**
     * Gets the current provider status
     */
    public ProviderStatus getProviderStatus() {
        return ProviderStatus.builder()
                .vertexAiEnabled(config.getVertexai().isEnabled() && vertexAiChatClient != null)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class ProviderStatus {
        private boolean vertexAiEnabled;
    }
}
