package com.dentistdss.workflow.controller;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import com.dentistdss.workflow.service.WorkflowMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for workflow migration operations
 * 
 * This controller provides endpoints for migrating existing approval workflows
 * from the legacy system to the new workflow engine.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@RestController
@RequestMapping("/workflow/migration")
@RequiredArgsConstructor
@Tag(name = "Workflow Migration", description = "Workflow migration APIs for legacy system integration")
public class WorkflowMigrationController {
    
    private final WorkflowMigrationService workflowMigrationService;
    
    @PostMapping("/user-approval")
    @Operation(summary = "Migrate user approval request", description = "Migrate an existing user approval request to the workflow engine")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> migrateUserApprovalRequest(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Requested role") @RequestParam String requestedRole,
            @Parameter(description = "Clinic ID") @RequestParam(required = false) Long clinicId,
            @Parameter(description = "Request reason") @RequestParam(required = false) String requestReason,
            @Parameter(description = "Current status") @RequestParam(defaultValue = "PENDING") String currentStatus) {
        
        ApiResponse<WorkflowInstanceResponse> response = workflowMigrationService.migrateUserApprovalRequest(
                userId, requestedRole, clinicId, requestReason, currentStatus);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/user-approval/from-existing")
    @Operation(summary = "Create workflow from existing request", description = "Create a workflow instance from existing approval request data")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> createWorkflowFromExistingRequest(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Requested role") @RequestParam String requestedRole,
            @Parameter(description = "Clinic ID") @RequestParam(required = false) Long clinicId,
            @Parameter(description = "Request reason") @RequestParam(required = false) String requestReason,
            @Parameter(description = "Current status") @RequestParam String currentStatus,
            @Parameter(description = "Reviewed by") @RequestParam(required = false) Long reviewedBy,
            @Parameter(description = "Review notes") @RequestParam(required = false) String reviewNotes) {
        
        ApiResponse<WorkflowInstanceResponse> response = workflowMigrationService.createWorkflowFromExistingRequest(
                userId, requestedRole, clinicId, requestReason, currentStatus, reviewedBy, reviewNotes);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/user-approval/batch")
    @Operation(summary = "Batch migrate approval requests", description = "Migrate multiple approval requests in a batch operation")
    public ResponseEntity<ApiResponse<String>> batchMigrateApprovalRequests(
            @RequestBody List<Map<String, Object>> approvalRequests) {
        
        ApiResponse<String> response = workflowMigrationService.batchMigrateApprovalRequests(approvalRequests);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate/{workflowInstanceId}")
    @Operation(summary = "Validate migration", description = "Validate that a workflow instance matches the original approval request")
    public ResponseEntity<ApiResponse<Boolean>> validateMigration(
            @Parameter(description = "Workflow instance ID") @PathVariable Long workflowInstanceId,
            @RequestBody Map<String, Object> originalRequest) {
        
        boolean isValid = workflowMigrationService.validateMigration(workflowInstanceId, originalRequest);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}
