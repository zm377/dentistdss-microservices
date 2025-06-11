package com.dentistdss.genai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FAQ Model for storing frequently asked questions and answers
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "faqs")
public class FAQ {
    
    @Id
    private String id;
    
    @TextIndexed(weight = 3)
    private String question;
    
    @TextIndexed(weight = 2)
    private String answer;
    
    @TextIndexed
    private List<String> keywords;
    
    private String category;
    
    private Integer priority; // Higher number = higher priority
    
    private Boolean isActive;
    
    private Long clinicId; // null for global FAQs
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private Integer viewCount = 0;
    
    @Builder.Default
    private Integer helpfulCount = 0;
    
    @Builder.Default
    private Integer notHelpfulCount = 0;
    
    public enum Category {
        GENERAL,
        APPOINTMENTS,
        SERVICES,
        BILLING,
        INSURANCE,
        CLINIC_HOURS,
        EMERGENCY,
        PROCEDURES,
        AFTERCARE,
        POLICIES
    }
}
