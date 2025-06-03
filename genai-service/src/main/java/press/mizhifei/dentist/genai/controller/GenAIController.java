package press.mizhifei.dentist.genai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.genai.model.Conversation;
import press.mizhifei.dentist.genai.repository.ConversationRepository;
import press.mizhifei.dentist.genai.service.ChatService;
import press.mizhifei.dentist.genai.service.TokenRateLimiter;
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
@RestController
@RequestMapping("/genai/chatbot")
@RequiredArgsConstructor
public class GenAIController {

    private final ChatService chatService;
    private final TokenRateLimiter tokenRateLimiter;
    private final ConversationRepository conversationRepository;
    private static final int MAX_HISTORY_MESSAGES = 10; // Max 5 turns (user + assistant)

    @PostMapping(value = "/help", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> help(@RequestBody String prompt, @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader) {
        String sid = sessionIdHeader == null ? UUID.randomUUID().toString() : sessionIdHeader;
        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(sid, tokens)) {
            return Flux.just(limitMessage());
        }
        // For "help" agent, apiProvidedContext can be used to fetch FAQ data if needed.
        // String apiProvidedContext = getApiContextForAgent("help", prompt);
        return streamAndPersist("help", sid, null, prompt, null);
    }

    @PostMapping(value = "/receptionist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> receptionist(@RequestBody String prompt, @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader) {
        String sid = sessionIdHeader == null ? UUID.randomUUID().toString() : sessionIdHeader;
        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(sid, tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("receptionist", prompt);
        return streamAndPersist("receptionist", sid, null, prompt, null);
    }

    @PostMapping(value = "/aidentist", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiDentist(@RequestBody String prompt, @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader) {
        String sid = sessionIdHeader == null ? UUID.randomUUID().toString() : sessionIdHeader;
        long tokens = estimateTokens(prompt);
        if (!tokenRateLimiter.tryConsume(sid, tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("aidentist", prompt);
        return streamAndPersist("aidentist", sid, null, prompt, null);
    }
    
    @PostMapping(value = "/triage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> triage(@RequestBody String symptoms, @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader) {
        String sid = sessionIdHeader == null ? UUID.randomUUID().toString() : sessionIdHeader;
        long tokens = estimateTokens(symptoms);
        if (!tokenRateLimiter.tryConsume(sid, tokens)) {
            return Flux.just(limitMessage());
        }
        // String apiProvidedContext = getApiContextForAgent("triage", symptoms);
        return streamAndPersist("triage", sid, null, symptoms, null);
    }

    @PostMapping(value = "/documentation/summarize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> summarizeDocumentation(@RequestBody String notes, @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader) {
        String sid = sessionIdHeader == null ? UUID.randomUUID().toString() : sessionIdHeader;
        long tokens = estimateTokens(notes);
        if (!tokenRateLimiter.tryConsume(sid, tokens)) {
            return Flux.just(limitMessage());
        }
        // For documentation, the notes themselves can be part of the apiProvidedContext or the main prompt
        // Depending on how ChatService is structured, it might be better to pass notes as 'prompt' and use context for specific instructions if any.
        return streamAndPersist("documentation", sid, null, notes, null);
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
                // String actualApiContext = (apiProvidedContext == null) ? getApiContextForAgent(agent, prompt) : apiProvidedContext;

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