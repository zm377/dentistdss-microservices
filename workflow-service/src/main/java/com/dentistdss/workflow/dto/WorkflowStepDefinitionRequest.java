package com.dentistdss.workflow.dto;

import com.dentistdss.workflow.model.StepType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating/updating workflow step definitions
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStepDefinitionRequest {
    
    @NotBlank(message = "Step name is required")
    private String stepName;
    
    @NotNull(message = "Step order is required")
    @Positive(message = "Step order must be positive")
    private Integer stepOrder;
    
    @NotNull(message = "Step type is required")
    private StepType stepType;
    
    private String description;
    
    @Builder.Default
    private Boolean isRequired = true;
    
    @Builder.Default
    private Boolean isParallel = false;
    
    private Integer timeoutMinutes;
    
    @Builder.Default
    private Integer retryAttempts = 0;
    
    private String conditionExpression;
    
    private String[] approvalRoles;
    
    private String serviceEndpoint;
    
    private String notificationTemplate;
    
    private Map<String, Object> stepConfiguration;
    
    private Map<String, Object> inputMapping;
    
    private Map<String, Object> outputMapping;
}
