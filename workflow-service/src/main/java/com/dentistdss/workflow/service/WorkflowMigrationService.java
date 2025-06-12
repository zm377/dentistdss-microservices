package com.dentistdss.workflow.service;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for migrating existing approval workflows to the new workflow engine
 * 
 * This service provides migration utilities to move existing user approval
 * requests from the user-profile-service to the new workflow-service.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowMigrationService {
    
    private final UserApprovalWorkflowService userApprovalWorkflowService;
    
    /**
     * Migrate existing user approval request to workflow engine
     * 
     * This method can be called from the user-profile-service to migrate
     * existing approval requests to the new workflow system.
     */
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> migrateUserApprovalRequest(
            Long userId, 
            String requestedRole, 
            Long clinicId, 
            String requestReason,
            String currentStatus) {
        
        log.info("Migrating user approval request for user: {} with role: {}", userId, requestedRole);
        
        try {
            // Determine the appropriate workflow based on role and clinic
            ApiResponse<WorkflowInstanceResponse> response;
            
            if ("CLINIC_ADMIN".equals(requestedRole) && clinicId != null) {
                // Migrate clinic admin approval
                response = userApprovalWorkflowService.createClinicAdminApprovalRequest(
                        userId, clinicId, "Migrated Clinic");
            } else if (("DENTIST".equals(requestedRole) || "RECEPTIONIST".equals(requestedRole)) && clinicId != null) {
                // Migrate staff approval
                response = userApprovalWorkflowService.createStaffApprovalRequest(
                        userId, clinicId, "Migrated Clinic", requestedRole);
            } else {
                // Migrate general user approval
                response = userApprovalWorkflowService.createApprovalRequest(
                        userId, requestReason, requestedRole);
            }
            
            if (response.isSuccess()) {
                log.info("Successfully migrated approval request for user: {} to workflow instance: {}", 
                        userId, response.getData().getId());
                
                // If the original request was already approved/rejected, we might need to handle that
                if (!"PENDING".equals(currentStatus)) {
                    log.info("Original request status was: {}, workflow will need manual intervention", currentStatus);
                }
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to migrate user approval request for user: {} - {}", userId, e.getMessage(), e);
            return ApiResponse.error("Migration failed: " + e.getMessage());
        }
    }
    
    /**
     * Create a workflow instance from existing approval request data
     * 
     * This method creates a workflow instance that matches the state of an existing
     * approval request, useful for maintaining continuity during migration.
     */
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> createWorkflowFromExistingRequest(
            Long userId,
            String requestedRole,
            Long clinicId,
            String requestReason,
            String currentStatus,
            Long reviewedBy,
            String reviewNotes) {
        
        log.info("Creating workflow from existing request for user: {}", userId);
        
        // First create the workflow
        ApiResponse<WorkflowInstanceResponse> response = migrateUserApprovalRequest(
                userId, requestedRole, clinicId, requestReason, currentStatus);
        
        if (!response.isSuccess()) {
            return response;
        }
        
        WorkflowInstanceResponse workflowInstance = response.getData();
        
        // If the original request was already processed, we need to update the workflow state
        if ("APPROVED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
            log.info("Original request was {}, updating workflow state accordingly", currentStatus);
            
            // In a real implementation, you would:
            // 1. Find the approval step in the workflow
            // 2. Update its status to match the original decision
            // 3. Complete the workflow if it was approved
            // 4. Fail the workflow if it was rejected
            
            // For now, we'll just log this as it requires more complex state management
            log.warn("Manual intervention required for workflow {} - original status: {}", 
                    workflowInstance.getId(), currentStatus);
        }
        
        return response;
    }
    
    /**
     * Batch migrate multiple approval requests
     * 
     * This method can be used to migrate multiple existing approval requests
     * in a batch operation.
     */
    @Transactional
    public ApiResponse<String> batchMigrateApprovalRequests(
            java.util.List<Map<String, Object>> approvalRequests) {
        
        log.info("Starting batch migration of {} approval requests", approvalRequests.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Map<String, Object> request : approvalRequests) {
            try {
                Long userId = (Long) request.get("userId");
                String requestedRole = (String) request.get("requestedRole");
                Long clinicId = (Long) request.get("clinicId");
                String requestReason = (String) request.get("requestReason");
                String currentStatus = (String) request.get("status");
                
                ApiResponse<WorkflowInstanceResponse> result = migrateUserApprovalRequest(
                        userId, requestedRole, clinicId, requestReason, currentStatus);
                
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                    log.error("Failed to migrate request for user {}: {}", userId, result.getMessage());
                }
                
            } catch (Exception e) {
                failureCount++;
                log.error("Error processing migration request: {}", e.getMessage(), e);
            }
        }
        
        String message = String.format("Batch migration completed. Success: %d, Failures: %d", 
                successCount, failureCount);
        log.info(message);
        
        return ApiResponse.successMessage(message);
    }
    
    /**
     * Validate that a workflow instance matches an existing approval request
     * 
     * This method can be used to verify that the migration was successful.
     */
    public boolean validateMigration(Long workflowInstanceId, Map<String, Object> originalRequest) {
        // Implementation would compare the workflow instance data with the original request
        // to ensure the migration was accurate
        log.info("Validating migration for workflow instance: {}", workflowInstanceId);
        
        // For now, return true as a placeholder
        return true;
    }
}
