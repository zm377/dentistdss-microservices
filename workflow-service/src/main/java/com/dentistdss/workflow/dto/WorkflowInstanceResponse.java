package com.dentistdss.workflow.dto;

import com.dentistdss.workflow.model.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for workflow instances
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowInstanceResponse {
    
    private Long id;
    private String workflowName;
    private String workflowDisplayName;
    private Integer workflowVersion;
    private String instanceName;
    private WorkflowStatus status;
    private String businessKey;
    private String entityType;
    private Long entityId;
    private Integer priority;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private Map<String, Object> contextData;
    private Integer currentStepOrder;
    private String currentStepName;
    private String errorMessage;
    private Integer retryCount;
    private Long startedBy;
    private String startedByName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime timeoutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkflowExecutionResponse> executions;
}
