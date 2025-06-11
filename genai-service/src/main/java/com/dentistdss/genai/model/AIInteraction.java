package com.dentistdss.genai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_interactions")
public class AIInteraction {
    
    @Id
    private String id;
    
    private Long userId;
    
    private UUID sessionId;
    
    private String interactionType; // TRIAGE, FAQ, DOCUMENTATION, DECISION_SUPPORT
    
    private String aiModel; // help_desk, receptionist, ai_dentist
    
    private String inputText;
    
    private String aiResponse;
    
    private Integer tokensUsed;
    
    private Integer responseTimeMs;
    
    private Integer feedbackRating;
    
    private String feedbackComment;
    
    private Map<String, Object> metadata;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
} 