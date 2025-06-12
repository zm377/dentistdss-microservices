package com.dentistdss.workflow.controller;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.StepApprovalRequest;
import com.dentistdss.workflow.dto.WorkflowInstanceRequest;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import com.dentistdss.workflow.model.WorkflowStatus;
import com.dentistdss.workflow.service.WorkflowExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for workflow execution management
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@RestController
@RequestMapping("/workflow/execution")
@RequiredArgsConstructor
@Tag(name = "Workflow Execution", description = "Workflow execution and monitoring APIs")
public class WorkflowExecutionController {
    
    private final WorkflowExecutionService workflowExecutionService;
    
    @PostMapping("/start")
    @Operation(summary = "Start workflow", description = "Start a new workflow instance")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startWorkflow(
            @Valid @RequestBody WorkflowInstanceRequest request,
            @Parameter(description = "User ID starting the workflow") @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        ApiResponse<WorkflowInstanceResponse> response = workflowExecutionService.startWorkflow(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{instanceId}/start")
    @Operation(summary = "Start workflow execution", description = "Start execution of a created workflow instance")
    public ResponseEntity<ApiResponse<String>> startWorkflowExecution(
            @Parameter(description = "Workflow instance ID") @PathVariable Long instanceId) {
        
        ApiResponse<String> response = workflowExecutionService.startWorkflowExecution(instanceId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{instanceId}")
    @Operation(summary = "Get workflow instance", description = "Get workflow instance details")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getWorkflowInstance(
            @Parameter(description = "Workflow instance ID") @PathVariable Long instanceId) {
        
        ApiResponse<WorkflowInstanceResponse> response = workflowExecutionService.getWorkflowInstance(instanceId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get workflow instances by status", description = "Get workflow instances filtered by status")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> getWorkflowInstancesByStatus(
            @Parameter(description = "Workflow status") @PathVariable WorkflowStatus status) {
        
        ApiResponse<List<WorkflowInstanceResponse>> response = workflowExecutionService.getWorkflowInstancesByStatus(status);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/step/{executionId}/approve")
    @Operation(summary = "Approve workflow step", description = "Approve or reject a workflow step")
    public ResponseEntity<ApiResponse<String>> approveStep(
            @Parameter(description = "Workflow execution ID") @PathVariable Long executionId,
            @Valid @RequestBody StepApprovalRequest request,
            @Parameter(description = "User ID approving the step") @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        ApiResponse<String> response = workflowExecutionService.approveStep(executionId, request, userId);
        return ResponseEntity.ok(response);
    }
}
