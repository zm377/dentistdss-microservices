package press.mizhifei.dentist.genai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.genai.model.Conversation;
import press.mizhifei.dentist.genai.repository.ConversationRepository;
import press.mizhifei.dentist.genai.service.ChatService;
import press.mizhifei.dentist.genai.service.TokenRateLimiter;
import press.mizhifei.dentist.genai.service.UserContextService;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@RestController
@RequestMapping("/genai/chatbot")
@RequiredArgsConstructor
public class GenAIController {

    private final ChatService chatService;
    private final TokenRateLimiter tokenRateLimiter;
    private final ConversationRepository conversationRepository;
    private final UserContextService userContextService;
    private static final int MAX_HISTORY_MESSAGES = 10; // Max 5 turns (user + assistant)

    @PostMapping(value = "/help", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> help(@RequestBody String prompt, ServerHttpRequest request) {
        // Extract user context from headers
        UserContextService.UserContext userContext = userContextService.extractUserContext(request);

        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(userContext.getSessionId(), tokens)) {
            return Flux.just(limitMessage());
        }

        // Use enhanced streaming with context and orchestration
        return streamAndPersistWithContext("help", userContext.getSessionId(), userContext, prompt, null);
    }

    @PostMapping(value = "/receptionist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> receptionist(@RequestBody String prompt, ServerHttpRequest request) {
        // Extract user context from headers
        UserContextService.UserContext userContext = userContextService.extractUserContext(request);
        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(userContext.getSessionId(), tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("receptionist", prompt);
        return streamAndPersist("receptionist", userContext.getSessionId(), userContext.getUserId(), prompt, null);
    }

    @PostMapping(value = "/aidentist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiDentist(@RequestBody String prompt, ServerHttpRequest request) {
        // Extract user context from headers
        UserContextService.UserContext userContext = userContextService.extractUserContext(request);
        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(userContext.getSessionId(), tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("aidentist", prompt);
        return streamAndPersist("aidentist", userContext.getSessionId(), userContext.getUserId(), prompt, null);
    }
    
    @PostMapping(value = "/triage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> triage(@RequestBody String symptoms, ServerHttpRequest request) {
        // Extract user context from headers
        UserContextService.UserContext userContext = userContextService.extractUserContext(request);
        long tokens = estimateTokens(symptoms);
        if (!tokenRateLimiter.tryConsume(userContext.getSessionId(), tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("triage", symptoms);
        return streamAndPersist("triage", userContext.getSessionId(), userContext.getUserId(), symptoms, null);
    }

    @PostMapping(value = "/documentation/summarize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> summarizeDocumentation(@RequestBody String notes, ServerHttpRequest request) {
        // Extract user context from headers
        UserContextService.UserContext userContext = userContextService.extractUserContext(request);
        long tokens = estimateTokens(notes);
        if (!tokenRateLimiter.tryConsume(userContext.getSessionId(), tokens)) {
            return Flux.just(limitMessage());
        }
        // For documentation, the notes themselves can be part of the apiProvidedContext or the main prompt
        // Depending on how ChatService is structured, it might be better to pass notes as 'prompt' and use context for specific instructions if any.
        return streamAndPersist("documentation", userContext.getSessionId(), userContext.getUserId(), notes, null);
    }

    private Flux<String> streamAndPersist(String agent, String sessionId, String userId, String prompt, String apiProvidedContext) {
        return conversationRepository.findBySessionId(sessionId)
            .collectList()
            .flatMapMany(conversations -> {
                Conversation conversation;
                if (conversations.isEmpty()) {
                    conversation = new Conversation();
                    conversation.setSessionId(sessionId);
                    conversation.setUserId(userId);
                    conversation.setAgent(agent);
                    conversation.setMessages(new ArrayList<>());
                } else {
                    // Assuming we want the most recently created or updated conversation if multiple exist by chance
                    conversation = conversations.stream().max(Comparator.comparing(Conversation::getCreatedAt)).get();
                     // Update agent and userId if this session is now used by a different agent/user
                    conversation.setAgent(agent);
                    if (userId != null) conversation.setUserId(userId);
                }

                Conversation.Message userMsg = new Conversation.Message();
                userMsg.setRole("user");
                userMsg.setContent(prompt);
                userMsg.setTimestamp(Instant.now());
                conversation.getMessages().add(userMsg);

                List<Conversation.Message> historyForChatService = conversation.getMessages().size() > 1 ? 
                    conversation.getMessages().subList(Math.max(0, conversation.getMessages().size() - 1 - MAX_HISTORY_MESSAGES), conversation.getMessages().size() - 1) 
                    : Collections.emptyList();
                
                // Ensure an API context, even if null, is passed to ChatService
//                 String actualApiContext = (apiProvidedContext == null) ? getApiContextForAgent(agent, prompt) : apiProvidedContext;

                final Conversation finalConversation = conversation; // Effectively final for lambda
                StringBuilder assistantResponseAggregator = new StringBuilder();

                return chatService.streamChat(agent, prompt, historyForChatService, apiProvidedContext, UUID.fromString(sessionId), userId != null ? Long.parseLong(userId) : null)
                    .doOnNext(assistantResponseAggregator::append)
                    .doOnComplete(() -> {
                        Conversation.Message assistantMsg = new Conversation.Message();
                        assistantMsg.setRole("assistant");
                        assistantMsg.setContent(assistantResponseAggregator.toString());
                        assistantMsg.setTimestamp(Instant.now());
                        finalConversation.getMessages().add(assistantMsg);
                        finalConversation.setCreatedAt(Instant.now()); // Update timestamp for latest activity
                        conversationRepository.save(finalConversation).subscribe();
                    })
                    .doOnSubscribe(subscription -> {
                        // Save the conversation with the user message immediately
                        // This helps if streaming fails, we still have the user part.
                        // However, this means if saving assistant message fails, user message is duplicated on retry.
                        // Current setup: save user message first, then full convo after assistant response.
                        // To avoid duplication, ensure conversation is loaded and updated, not created anew if user message only saved.
                        // The current load logic (findBySessionId and picking latest) should handle this.
                        if (finalConversation.getMessages().size() == 1) { // Only user message added so far
                             conversationRepository.save(finalConversation).subscribe(); 
                        }
                    });
            });
    }

    /**
     * Enhanced streaming with user context and prompt orchestration
     */
    private Flux<String> streamAndPersistWithContext(String agent, String sessionId, UserContextService.UserContext userContext, String prompt, String apiProvidedContext) {
        String userId = userContext.getUserId();

        return conversationRepository.findBySessionId(sessionId)
            .collectList()
            .flatMapMany(conversations -> {
                Conversation conversation;
                if (conversations.isEmpty()) {
                    conversation = new Conversation();
                    conversation.setSessionId(sessionId);
                    conversation.setUserId(userId);
                    conversation.setAgent(agent);
                    conversation.setMessages(new ArrayList<>());
                } else {
                    conversation = conversations.stream().max(Comparator.comparing(Conversation::getCreatedAt)).get();
                    conversation.setAgent(agent);
                    if (userId != null) conversation.setUserId(userId);
                }

                Conversation.Message userMsg = new Conversation.Message();
                userMsg.setRole("user");
                userMsg.setContent(prompt);
                userMsg.setTimestamp(Instant.now());
                conversation.getMessages().add(userMsg);

                List<Conversation.Message> historyForChatService = conversation.getMessages().size() > 1 ?
                    conversation.getMessages().subList(Math.max(0, conversation.getMessages().size() - 1 - MAX_HISTORY_MESSAGES), conversation.getMessages().size() - 1)
                    : Collections.emptyList();

                final Conversation finalConversation = conversation;
                StringBuilder assistantResponseAggregator = new StringBuilder();

                // Use enhanced chat service with context and orchestration
                return chatService.streamChatWithContext(agent, prompt, historyForChatService, apiProvidedContext, userContext)
                    .doOnNext(assistantResponseAggregator::append)
                    .doOnComplete(() -> {
                        Conversation.Message assistantMsg = new Conversation.Message();
                        assistantMsg.setRole("assistant");
                        assistantMsg.setContent(assistantResponseAggregator.toString());
                        assistantMsg.setTimestamp(Instant.now());
                        finalConversation.getMessages().add(assistantMsg);
                        finalConversation.setCreatedAt(Instant.now());
                        conversationRepository.save(finalConversation).subscribe();
                    })
                    .doOnSubscribe(subscription -> {
                        if (finalConversation.getMessages().size() == 1) {
                             conversationRepository.save(finalConversation).subscribe();
                        }
                    })
                    .doOnError(error -> {
                        log.error("Error in streamAndPersistWithContext for agent {}: {}", agent, error.getMessage());
                    });
            });
    }

    // private String getApiContextForAgent(String agent, String userInput) {
    //     if ("help".equalsIgnoreCase(agent)) {
    //         // TODO: Implement logic to query FAQ database based on userInput
    //         // e.g., if (userInput.contains("clinic hours")) return "ClinicPolicy: Our clinic is open 9 AM to 5 PM.";
    //         return null; // Placeholder
    //     }
    //     // Add other agent-specific context fetching here
    //     return null;
    // }

    private long estimateTokens(String text) {
        return Math.max(1, text.length() / 4);
    }

    private String limitMessage() {
        return "You have reached the maximal inquiries. For a better user experience for every one of our users, we kindly suggest you ask more questions 3 minutes later.";
    }
} 