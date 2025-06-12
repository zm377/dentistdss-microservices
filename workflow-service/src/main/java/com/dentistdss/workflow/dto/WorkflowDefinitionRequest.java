package com.dentistdss.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating/updating workflow definitions
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDefinitionRequest {
    
    @NotBlank(message = "Workflow name is required")
    private String name;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private String description;
    
    @NotNull(message = "Version is required")
    @Positive(message = "Version must be positive")
    private Integer version;
    
    private String category;
    
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private Boolean isSystemWorkflow = false;
    
    private Integer timeoutMinutes;
    
    @Builder.Default
    private Integer maxRetryAttempts = 3;
    
    @Builder.Default
    private Boolean autoStart = false;
    
    @Builder.Default
    private Boolean requiresApproval = false;
    
    private Map<String, Object> configuration;
    
    private Map<String, Object> inputSchema;
    
    private Map<String, Object> outputSchema;
    
    private List<WorkflowStepDefinitionRequest> steps;
}
