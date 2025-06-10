package press.mizhifei.dentist.genai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.genai.model.AIInteraction;
import press.mizhifei.dentist.genai.model.Conversation;
import press.mizhifei.dentist.genai.repository.AIInteractionRepository;
import press.mizhifei.dentist.genai.service.UserContextService.UserContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final AIPromptService promptService;
    private final AIInteractionRepository aiInteractionRepository;
    private final PromptOrchestrationService promptOrchestrationService;
    private final AIProviderService aiProviderService;

    public Flux<String> streamChat(String agent, String userPrompt, List<Conversation.Message> history, String apiProvidedContext, UUID sessionId, Long userId) {
        Instant startTime = Instant.now();
        StringBuilder responseBuilder = new StringBuilder();
        List<Message> messages = buildMessages(agent, userPrompt, history, apiProvidedContext);

        return aiProviderService
                .streamChat(new Prompt(messages))
                .doOnNext(responseBuilder::append)
                .doOnComplete(() -> {
                    saveInteraction(agent, userPrompt, responseBuilder.toString(),
                            sessionId, userId, startTime, apiProvidedContext, history != null ? history.size() : 0).subscribe();
                })
                .doOnError(error -> {
                    log.error("Error in chat streaming for agent {}: {}", agent, error.getMessage());
                });
    }

    /**
     * Enhanced streaming chat with user context and prompt orchestration
     */
    public Flux<String> streamChatWithContext(String agent, String userPrompt, List<Conversation.Message> history,
                                            String apiProvidedContext, UserContext userContext) {
        Instant startTime = Instant.now();
        StringBuilder responseBuilder = new StringBuilder();

        return promptOrchestrationService.orchestratePrompt(agent, userContext, userPrompt, apiProvidedContext)
                .flatMapMany(orchestratedPrompt -> {
                    List<Message> messages = buildMessagesWithOrchestration(orchestratedPrompt, userPrompt, history);

                    return aiProviderService
                            .streamChat(new Prompt(messages))
                            .doOnNext(responseBuilder::append)
                            .doOnComplete(() -> {
                                UUID sessionId = UUID.fromString(userContext.getSessionId());
                                Long userId = userContext.getUserId() != null ?
                                        Long.parseLong(userContext.getUserId()) : null;

                                saveInteraction(agent, userPrompt, responseBuilder.toString(),
                                        sessionId, userId, startTime, apiProvidedContext,
                                        history != null ? history.size() : 0).subscribe();
                            });
                })
                .doOnError(error -> {
                    log.error("Error in enhanced chat streaming for agent {}: {}", agent, error.getMessage());
                });
    }

    public Mono<String> chat(String agent, String userPrompt, List<Conversation.Message> history, String apiProvidedContext, UUID sessionId, Long userId) {
        Instant startTime = Instant.now();
        List<Message> messages = buildMessages(agent, userPrompt, history, apiProvidedContext);
        
        return Mono.fromSupplier(() -> chatClient
                .prompt(new Prompt(messages))
                .call()
                .content())
                .flatMap(response -> 
                    saveInteraction(agent, userPrompt, response, sessionId, userId, startTime, apiProvidedContext, history != null ? history.size() : 0)
                            .thenReturn(response)
                )
                .doOnError(error -> {
                    log.error("Error in chat for agent {}: {}", agent, error.getMessage());
                });
    }

    private List<Message> buildMessages(String agent, String userPrompt, List<Conversation.Message> history, String apiProvidedContext) {
        List<Message> messages = new ArrayList<>();
        String systemPromptContent = promptService.getSystemPrompt(agent);

        if (apiProvidedContext != null && !apiProvidedContext.isEmpty()) {
            systemPromptContent += "\n\nContext: " + apiProvidedContext;
        }
        messages.add(new SystemMessage(systemPromptContent));

        if (history != null) {
            for (Conversation.Message msg : history) {
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }
        messages.add(new UserMessage(userPrompt)); // Current user prompt
        return messages;
    }

    /**
     * Builds messages with orchestrated system prompt
     */
    private List<Message> buildMessagesWithOrchestration(String orchestratedSystemPrompt, String userPrompt, List<Conversation.Message> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(orchestratedSystemPrompt));

        if (history != null) {
            for (Conversation.Message msg : history) {
                if ("user".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }
        messages.add(new UserMessage(userPrompt)); // Current user prompt
        return messages;
    }
    
    // Backward compatibility methods
    public Flux<String> streamChat(String agent, String userPrompt, String apiProvidedContext) {
        // For backward compatibility, session ID is random, userId is null, history is empty.
        return streamChat(agent, userPrompt, new ArrayList<>(), apiProvidedContext, UUID.randomUUID(), null);
    }
    
    public Mono<String> chat(String agent, String userPrompt, String apiProvidedContext) {
        // For backward compatibility, session ID is random, userId is null, history is empty.
        return chat(agent, userPrompt, new ArrayList<>(), apiProvidedContext, UUID.randomUUID(), null);
    }
        
    private Mono<AIInteraction> saveInteraction(String agent, String userPrompt, String response, 
                                               UUID sessionId, Long userId, Instant startTime, 
                                               String apiProvidedContext, int historySize) {
        
        String interactionType = determineInteractionType(agent);
        Map<String, Object> metadata = new HashMap<>();
        if (apiProvidedContext != null && !apiProvidedContext.isEmpty()) {
            metadata.put("apiProvidedContext", apiProvidedContext);
        }
        metadata.put("historySize", historySize);
        // crude token estimation (4 chars per token for prompt + response)
        long estimatedTokens = (userPrompt.length() + response.length()) / 4L;
        
        long responseTimeMs = Duration.between(startTime, Instant.now()).toMillis();
        
        AIInteraction interaction = AIInteraction.builder()
                .userId(userId)
                .sessionId(sessionId)
                .interactionType(interactionType)
                .aiModel(agent)
                .inputText(userPrompt)
                .aiResponse(response)
                .tokensUsed((int) estimatedTokens) // Add estimated tokens
                .responseTimeMs((int) responseTimeMs)
                .metadata(metadata)
                .build();
        
        return aiInteractionRepository.save(interaction)
                .doOnSuccess(saved -> log.debug("Saved AI interaction: {}", saved.getId()))
                .doOnError(error -> log.error("Failed to save AI interaction for agent {}: {}", agent, error.getMessage()));
    }
    
    private String determineInteractionType(String agent) {
        return switch (agent.toLowerCase()) {
            case "triage" -> "TRIAGE";
            case "help" -> "FAQ"; // Assuming 'help' maps to FAQ
            case "receptionist" -> "RECEPTIONIST_CHAT"; // Example, can be more specific
            case "aidentist" -> "DECISION_SUPPORT";
            case "documentation" -> "DOCUMENTATION_ASSISTANCE";
            default -> "GENERAL_CHAT"; // A general category for others
        };
    }
} 