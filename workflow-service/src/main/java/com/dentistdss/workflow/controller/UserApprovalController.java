package com.dentistdss.workflow.controller;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import com.dentistdss.workflow.service.UserApprovalWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user approval workflows
 * 
 * This controller provides backward compatibility with the existing user approval system
 * while leveraging the new workflow engine.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@RestController
@RequestMapping("/workflow/user-approval")
@RequiredArgsConstructor
@Tag(name = "User Approval Workflow", description = "User approval workflow management APIs")
public class UserApprovalController {
    
    private final UserApprovalWorkflowService userApprovalWorkflowService;
    
    @PostMapping("/request")
    @Operation(summary = "Create approval request", description = "Create a new user approval request")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> createApprovalRequest(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Request reason") @RequestParam(required = false) String requestReason,
            @Parameter(description = "User role") @RequestParam(required = false, defaultValue = "PATIENT") String userRole) {
        
        ApiResponse<WorkflowInstanceResponse> response = userApprovalWorkflowService.createApprovalRequest(userId, requestReason, userRole);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/clinic-admin/request")
    @Operation(summary = "Create clinic admin approval request", description = "Create approval request for clinic admin signup")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> createClinicAdminApprovalRequest(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Clinic ID") @RequestParam Long clinicId,
            @Parameter(description = "Clinic name") @RequestParam String clinicName) {
        
        ApiResponse<WorkflowInstanceResponse> response = userApprovalWorkflowService.createClinicAdminApprovalRequest(userId, clinicId, clinicName);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/staff/request")
    @Operation(summary = "Create staff approval request", description = "Create approval request for staff signup")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> createStaffApprovalRequest(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Clinic ID") @RequestParam Long clinicId,
            @Parameter(description = "Clinic name") @RequestParam String clinicName,
            @Parameter(description = "Staff role") @RequestParam String role) {
        
        ApiResponse<WorkflowInstanceResponse> response = userApprovalWorkflowService.createStaffApprovalRequest(userId, clinicId, clinicName, role);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending approval requests", description = "Get pending approval requests for the current user")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> getPendingApprovalRequests(
            @Parameter(description = "User ID") @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @Parameter(description = "User role") @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        ApiResponse<List<WorkflowInstanceResponse>> response = userApprovalWorkflowService.getPendingApprovalRequests(userId, userRole);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{userId}")
    @Operation(summary = "Get user approval history", description = "Get approval history for a specific user")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> getUserApprovalHistory(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        ApiResponse<List<WorkflowInstanceResponse>> response = userApprovalWorkflowService.getUserApprovalHistory(userId);
        return ResponseEntity.ok(response);
    }
}
