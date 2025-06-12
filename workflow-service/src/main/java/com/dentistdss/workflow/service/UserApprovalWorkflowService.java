package com.dentistdss.workflow.service;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowInstanceRequest;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing user approval workflows
 * 
 * This service provides a bridge between the existing user approval system
 * and the new workflow engine, maintaining backward compatibility while
 * migrating to the centralized workflow management.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApprovalWorkflowService {
    
    private final WorkflowExecutionService workflowExecutionService;
    private final WorkflowDefinitionService workflowDefinitionService;
    
    private static final String USER_APPROVAL_WORKFLOW = "user_approval_workflow";
    private static final String CLINIC_ADMIN_APPROVAL_WORKFLOW = "clinic_admin_approval_workflow";
    private static final String STAFF_APPROVAL_WORKFLOW = "staff_approval_workflow";
    
    /**
     * Create approval request for user signup
     * This method replaces the existing createApprovalRequest in UserApprovalService
     */
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> createApprovalRequest(Long userId, String requestReason, String userRole) {
        log.info("Creating approval request for user: {} with role: {}", userId, userRole);
        
        // Determine workflow type based on user role
        String workflowName = determineWorkflowName(userRole);
        
        // Prepare workflow input data
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userId", userId);
        inputData.put("requestReason", requestReason);
        inputData.put("requestedRole", userRole);
        inputData.put("requestType", "USER_SIGNUP");
        inputData.put("createdAt", java.time.LocalDateTime.now());
        
        // Create workflow instance request
        WorkflowInstanceRequest workflowRequest = WorkflowInstanceRequest.builder()
                .workflowName(workflowName)
                .instanceName("User Approval - " + userId)
                .businessKey("user_approval_" + userId)
                .entityType("USER")
                .entityId(userId)
                .priority(5)
                .inputData(inputData)
                .autoStart(true)
                .build();
        
        // Start the workflow
        return workflowExecutionService.startWorkflow(workflowRequest, userId);
    }
    
    /**
     * Create approval request for clinic admin signup
     */
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> createClinicAdminApprovalRequest(Long userId, Long clinicId, String clinicName) {
        log.info("Creating clinic admin approval request for user: {} and clinic: {}", userId, clinicId);
        
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userId", userId);
        inputData.put("clinicId", clinicId);
        inputData.put("clinicName", clinicName);
        inputData.put("requestedRole", "CLINIC_ADMIN");
        inputData.put("requestType", "CLINIC_ADMIN_SIGNUP");
        inputData.put("requestReason", "Clinic admin sign up for " + clinicName);
        inputData.put("createdAt", java.time.LocalDateTime.now());
        
        WorkflowInstanceRequest workflowRequest = WorkflowInstanceRequest.builder()
                .workflowName(CLINIC_ADMIN_APPROVAL_WORKFLOW)
                .instanceName("Clinic Admin Approval - " + userId)
                .businessKey("clinic_admin_approval_" + userId + "_" + clinicId)
                .entityType("USER")
                .entityId(userId)
                .priority(3) // Higher priority for clinic admin approvals
                .inputData(inputData)
                .autoStart(true)
                .build();
        
        return workflowExecutionService.startWorkflow(workflowRequest, userId);
    }
    
    /**
     * Create approval request for staff signup
     */
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> createStaffApprovalRequest(Long userId, Long clinicId, String clinicName, String role) {
        log.info("Creating staff approval request for user: {} at clinic: {} with role: {}", userId, clinicId, role);
        
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("userId", userId);
        inputData.put("clinicId", clinicId);
        inputData.put("clinicName", clinicName);
        inputData.put("requestedRole", role);
        inputData.put("requestType", "STAFF_SIGNUP");
        inputData.put("requestReason", "Clinic staff sign up for " + clinicName);
        inputData.put("createdAt", java.time.LocalDateTime.now());
        
        WorkflowInstanceRequest workflowRequest = WorkflowInstanceRequest.builder()
                .workflowName(STAFF_APPROVAL_WORKFLOW)
                .instanceName("Staff Approval - " + userId)
                .businessKey("staff_approval_" + userId + "_" + clinicId)
                .entityType("USER")
                .entityId(userId)
                .priority(4)
                .inputData(inputData)
                .autoStart(true)
                .build();
        
        return workflowExecutionService.startWorkflow(workflowRequest, userId);
    }
    
    /**
     * Get pending approval requests for a user (replaces existing method)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowInstanceResponse>> getPendingApprovalRequests(Long userId, String userRole) {
        log.info("Getting pending approval requests for user: {} with role: {}", userId, userRole);
        
        // Get all pending workflow instances
        ApiResponse<List<WorkflowInstanceResponse>> response = workflowExecutionService.getWorkflowInstancesByStatus(
                com.dentistdss.workflow.model.WorkflowStatus.WAITING);
        
        if (!response.isSuccess()) {
            return response;
        }
        
        // Filter based on user role and permissions
        List<WorkflowInstanceResponse> filteredInstances = response.getData().stream()
                .filter(instance -> canUserApprove(instance, userId, userRole))
                .toList();
        
        return ApiResponse.success(filteredInstances);
    }
    
    /**
     * Get approval history for a specific user
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowInstanceResponse>> getUserApprovalHistory(Long userId) {
        log.info("Getting approval history for user: {}", userId);
        
        // This would need to be implemented to filter workflow instances by entity ID
        // For now, return empty list as placeholder
        return ApiResponse.success(List.of());
    }
    
    /**
     * Check if workflow definitions exist, create them if not
     */
    @Transactional
    public void ensureWorkflowDefinitionsExist() {
        log.info("Ensuring user approval workflow definitions exist");
        
        // Check if user approval workflow exists
        ApiResponse<com.dentistdss.workflow.model.WorkflowDefinition> userWorkflow = 
                workflowDefinitionService.getWorkflowDefinitionByName(USER_APPROVAL_WORKFLOW);
        
        if (!userWorkflow.isSuccess()) {
            createUserApprovalWorkflowDefinition();
        }
        
        // Check if clinic admin approval workflow exists
        ApiResponse<com.dentistdss.workflow.model.WorkflowDefinition> clinicAdminWorkflow = 
                workflowDefinitionService.getWorkflowDefinitionByName(CLINIC_ADMIN_APPROVAL_WORKFLOW);
        
        if (!clinicAdminWorkflow.isSuccess()) {
            createClinicAdminApprovalWorkflowDefinition();
        }
        
        // Check if staff approval workflow exists
        ApiResponse<com.dentistdss.workflow.model.WorkflowDefinition> staffWorkflow = 
                workflowDefinitionService.getWorkflowDefinitionByName(STAFF_APPROVAL_WORKFLOW);
        
        if (!staffWorkflow.isSuccess()) {
            createStaffApprovalWorkflowDefinition();
        }
    }
    
    private String determineWorkflowName(String userRole) {
        return switch (userRole) {
            case "CLINIC_ADMIN" -> CLINIC_ADMIN_APPROVAL_WORKFLOW;
            case "DENTIST", "RECEPTIONIST" -> STAFF_APPROVAL_WORKFLOW;
            default -> USER_APPROVAL_WORKFLOW;
        };
    }
    
    private boolean canUserApprove(WorkflowInstanceResponse instance, Long userId, String userRole) {
        // System admins can approve all requests
        if ("SYSTEM_ADMIN".equals(userRole)) {
            return true;
        }
        
        // Clinic admins can approve staff requests for their clinic
        if ("CLINIC_ADMIN".equals(userRole) && instance.getInputData() != null) {
            Object clinicId = instance.getInputData().get("clinicId");
            // Would need to check if user is admin of this clinic
            return clinicId != null;
        }
        
        return false;
    }
    
    private void createUserApprovalWorkflowDefinition() {
        log.info("Creating user approval workflow definition");

        com.dentistdss.workflow.dto.WorkflowDefinitionRequest request =
                com.dentistdss.workflow.dto.WorkflowDefinitionRequest.builder()
                .name(USER_APPROVAL_WORKFLOW)
                .displayName("User Approval Workflow")
                .description("Standard user approval workflow for patient registrations")
                .version(1)
                .category("USER_MANAGEMENT")
                .isActive(true)
                .isSystemWorkflow(true)
                .timeoutMinutes(10080) // 7 days
                .maxRetryAttempts(3)
                .autoStart(true)
                .requiresApproval(true)
                .steps(createUserApprovalSteps())
                .build();

        workflowDefinitionService.createWorkflowDefinition(request, 1L); // System user
    }

    private void createClinicAdminApprovalWorkflowDefinition() {
        log.info("Creating clinic admin approval workflow definition");

        com.dentistdss.workflow.dto.WorkflowDefinitionRequest request =
                com.dentistdss.workflow.dto.WorkflowDefinitionRequest.builder()
                .name(CLINIC_ADMIN_APPROVAL_WORKFLOW)
                .displayName("Clinic Admin Approval Workflow")
                .description("Approval workflow for clinic administrator registrations")
                .version(1)
                .category("USER_MANAGEMENT")
                .isActive(true)
                .isSystemWorkflow(true)
                .timeoutMinutes(10080) // 7 days
                .maxRetryAttempts(3)
                .autoStart(true)
                .requiresApproval(true)
                .steps(createClinicAdminApprovalSteps())
                .build();

        workflowDefinitionService.createWorkflowDefinition(request, 1L); // System user
    }

    private void createStaffApprovalWorkflowDefinition() {
        log.info("Creating staff approval workflow definition");

        com.dentistdss.workflow.dto.WorkflowDefinitionRequest request =
                com.dentistdss.workflow.dto.WorkflowDefinitionRequest.builder()
                .name(STAFF_APPROVAL_WORKFLOW)
                .displayName("Staff Approval Workflow")
                .description("Approval workflow for clinic staff registrations")
                .version(1)
                .category("USER_MANAGEMENT")
                .isActive(true)
                .isSystemWorkflow(true)
                .timeoutMinutes(10080) // 7 days
                .maxRetryAttempts(3)
                .autoStart(true)
                .requiresApproval(true)
                .steps(createStaffApprovalSteps())
                .build();

        workflowDefinitionService.createWorkflowDefinition(request, 1L); // System user
    }

    private List<com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest> createUserApprovalSteps() {
        return List.of(
            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("validate_user_data")
                .stepOrder(1)
                .stepType(com.dentistdss.workflow.model.StepType.AUTOMATED)
                .description("Validate user registration data")
                .isRequired(true)
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_approval_notification")
                .stepOrder(2)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send notification to system admins for approval")
                .isRequired(true)
                .notificationTemplate("user_approval_request")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("await_system_admin_approval")
                .stepOrder(3)
                .stepType(com.dentistdss.workflow.model.StepType.MANUAL_APPROVAL)
                .description("Wait for system administrator approval")
                .isRequired(true)
                .approvalRoles(new String[]{"SYSTEM_ADMIN"})
                .timeoutMinutes(10080) // 7 days
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("update_user_status")
                .stepOrder(4)
                .stepType(com.dentistdss.workflow.model.StepType.SERVICE_CALL)
                .description("Update user approval status in auth service")
                .isRequired(true)
                .serviceEndpoint("auth-service/user/{userId}/approval")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_approval_result")
                .stepOrder(5)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send approval result notification to user")
                .isRequired(true)
                .notificationTemplate("user_approval_result")
                .timeoutMinutes(5)
                .build()
        );
    }

    private List<com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest> createClinicAdminApprovalSteps() {
        return List.of(
            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("validate_clinic_admin_data")
                .stepOrder(1)
                .stepType(com.dentistdss.workflow.model.StepType.AUTOMATED)
                .description("Validate clinic admin registration data")
                .isRequired(true)
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_clinic_admin_approval_notification")
                .stepOrder(2)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send notification to system admins for clinic admin approval")
                .isRequired(true)
                .notificationTemplate("clinic_admin_approval_request")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("await_system_admin_approval")
                .stepOrder(3)
                .stepType(com.dentistdss.workflow.model.StepType.MANUAL_APPROVAL)
                .description("Wait for system administrator approval")
                .isRequired(true)
                .approvalRoles(new String[]{"SYSTEM_ADMIN"})
                .timeoutMinutes(10080) // 7 days
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("update_user_status")
                .stepOrder(4)
                .stepType(com.dentistdss.workflow.model.StepType.SERVICE_CALL)
                .description("Update user approval status in auth service")
                .isRequired(true)
                .serviceEndpoint("auth-service/user/{userId}/approval")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("update_clinic_status")
                .stepOrder(5)
                .stepType(com.dentistdss.workflow.model.StepType.SERVICE_CALL)
                .description("Update clinic approval status in auth service")
                .isRequired(true)
                .serviceEndpoint("auth-service/clinic/{clinicId}/approval")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_approval_result")
                .stepOrder(6)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send approval result notification to clinic admin")
                .isRequired(true)
                .notificationTemplate("clinic_admin_approval_result")
                .timeoutMinutes(5)
                .build()
        );
    }

    private List<com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest> createStaffApprovalSteps() {
        return List.of(
            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("validate_staff_data")
                .stepOrder(1)
                .stepType(com.dentistdss.workflow.model.StepType.AUTOMATED)
                .description("Validate staff registration data")
                .isRequired(true)
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_clinic_admin_notification")
                .stepOrder(2)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send notification to clinic admin for staff approval")
                .isRequired(true)
                .notificationTemplate("staff_approval_request")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("await_clinic_admin_approval")
                .stepOrder(3)
                .stepType(com.dentistdss.workflow.model.StepType.MANUAL_APPROVAL)
                .description("Wait for clinic administrator approval")
                .isRequired(true)
                .approvalRoles(new String[]{"CLINIC_ADMIN"})
                .timeoutMinutes(4320) // 3 days
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("update_user_status")
                .stepOrder(4)
                .stepType(com.dentistdss.workflow.model.StepType.SERVICE_CALL)
                .description("Update user approval status in auth service")
                .isRequired(true)
                .serviceEndpoint("auth-service/user/{userId}/approval")
                .timeoutMinutes(5)
                .build(),

            com.dentistdss.workflow.dto.WorkflowStepDefinitionRequest.builder()
                .stepName("send_approval_result")
                .stepOrder(5)
                .stepType(com.dentistdss.workflow.model.StepType.NOTIFICATION)
                .description("Send approval result notification to staff member")
                .isRequired(true)
                .notificationTemplate("staff_approval_result")
                .timeoutMinutes(5)
                .build()
        );
    }
}
