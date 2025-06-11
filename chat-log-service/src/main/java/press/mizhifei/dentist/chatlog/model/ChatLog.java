package press.mizhifei.dentist.chatlog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Chat Log Document - Main entity for storing conversational interactions
 * 
 * Stores all user-AI interactions with comprehensive metadata for:
 * - Conversation tracking and threading
 * - Token usage analytics
 * - PHI redaction compliance
 * - Audit and access logging
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_logs")
public class ChatLog {
    
    @Id
    private String id;
    
    /**
     * Unique session identifier for conversation threading
     */
    @Indexed
    private UUID sessionId;
    
    /**
     * User ID (nullable for anonymous users)
     */
    @Indexed
    private Long userId;
    
    /**
     * Clinic ID for data filtering and access control
     */
    @Indexed
    private Long clinicId;
    
    /**
     * Sequential message number within the session
     */
    private Integer messageSequence;
    
    /**
     * User's input message (potentially redacted)
     */
    private String userMessage;
    
    /**
     * AI's response message (potentially redacted)
     */
    private String aiResponse;
    
    /**
     * AI agent type (help, receptionist, aidentist, triage)
     */
    @Indexed
    private String agentType;
    
    /**
     * Interaction type (TRIAGE, FAQ, DOCUMENTATION, DECISION_SUPPORT)
     */
    @Indexed
    private String interactionType;
    
    /**
     * Token usage metrics
     */
    private TokenUsage tokenUsage;
    
    /**
     * PHI redaction information
     */
    private PHIRedactionInfo phiRedaction;
    
    /**
     * Client IP address for audit purposes
     */
    private String clientIpAddress;
    
    /**
     * User agent string for audit purposes
     */
    private String userAgent;
    
    /**
     * Additional metadata (context, preferences, etc.)
     */
    private Map<String, Object> metadata;
    
    /**
     * Timestamp when the interaction occurred
     */
    @Indexed
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Timestamp when the record was created
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Timestamp when the record was last updated
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
