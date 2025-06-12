package com.dentistdss.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for starting workflow instances
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowInstanceRequest {
    
    @NotBlank(message = "Workflow name is required")
    private String workflowName;
    
    private Integer workflowVersion;
    
    private String instanceName;
    
    private String businessKey;
    
    private String entityType;
    
    private Long entityId;
    
    @Builder.Default
    private Integer priority = 5;
    
    @NotNull(message = "Input data is required")
    private Map<String, Object> inputData;
    
    private Map<String, Object> contextData;
    
    @Builder.Default
    private Boolean autoStart = true;
}
