package com.dentistdss.genai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dynamic prompt template for AI agents with role-based and clinic-specific customization
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prompt_templates")
public class PromptTemplate {
    
    @Id
    private String id;
    
    /**
     * Agent type (help, receptionist, aidentist, triage, documentation)
     */
    private String agentType;
    
    /**
     * Template name for identification
     */
    private String templateName;
    
    /**
     * Base system prompt template with placeholders
     */
    private String systemPromptTemplate;
    
    /**
     * User context enhancement template
     */
    private String contextTemplate;
    
    /**
     * Role-based customizations
     * Key: role name (PATIENT, DENTIST, CLINIC_ADMIN, etc.)
     * Value: role-specific prompt additions
     */
    private Map<String, String> roleCustomizations;
    
    /**
     * Clinic-specific customizations
     * Key: clinic ID
     * Value: clinic-specific prompt additions
     */
    private Map<String, String> clinicCustomizations;
    
    /**
     * Template variables and their descriptions
     */
    private Map<String, String> templateVariables;
    
    /**
     * Priority for template selection (higher = more priority)
     */
    private Integer priority;
    
    /**
     * Whether this template is active
     */
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Supported user roles for this template
     */
    private List<String> supportedRoles;
    
    /**
     * Supported clinic IDs (empty means all clinics)
     */
    private List<String> supportedClinics;
    
    /**
     * Template version for tracking changes
     */
    @Builder.Default
    private String version = "1.0";
    
    /**
     * Template metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Creation timestamp
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Last update timestamp
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * Created by user ID
     */
    private String createdBy;
    
    /**
     * Last updated by user ID
     */
    private String updatedBy;
    
    /**
     * Template usage statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageStats {
        private Long totalUsage;
        private Long successfulResponses;
        private Long failedResponses;
        private Double averageResponseTime;
        private LocalDateTime lastUsed;
    }
    
    private UsageStats usageStats;
}
