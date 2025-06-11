package press.mizhifei.dentist.chatlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating new chat log entries
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatLogRequest {
    
    /**
     * Session ID for conversation threading (required)
     */
    @NotNull(message = "Session ID is required")
    private UUID sessionId;
    
    /**
     * User ID (optional for anonymous users)
     */
    private Long userId;
    
    /**
     * Clinic ID for data organization
     */
    private Long clinicId;
    
    /**
     * Sequential message number within the session
     */
    private Integer messageSequence;
    
    /**
     * User's input message (required)
     */
    @NotBlank(message = "User message cannot be blank")
    private String userMessage;
    
    /**
     * AI's response message (required)
     */
    @NotBlank(message = "AI response cannot be blank")
    private String aiResponse;
    
    /**
     * AI agent type (help, receptionist, aidentist, triage)
     */
    private String agentType;
    
    /**
     * Interaction type (TRIAGE, FAQ, DOCUMENTATION, DECISION_SUPPORT)
     */
    private String interactionType;
    
    /**
     * Token usage information
     */
    private TokenUsageRequest tokenUsage;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsageRequest {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private Double estimatedCost;
        private String model;
        private Long responseTimeMs;
    }
}
