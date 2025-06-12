package com.dentistdss.workflow.dto;

import com.dentistdss.workflow.model.StepStatus;
import com.dentistdss.workflow.model.StepType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for workflow executions
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowExecutionResponse {
    
    private Long id;
    private Long workflowInstanceId;
    private String stepName;
    private Integer stepOrder;
    private StepType stepType;
    private StepStatus status;
    private Map<String, Object> inputData;
    private Map<String, Object> outputData;
    private String errorMessage;
    private Integer retryCount;
    private Long assignedTo;
    private String assignedToName;
    private Long approvedBy;
    private String approvedByName;
    private String approvalNotes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime timeoutAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
