package com.dentistdss.workflow.service;

import com.dentistdss.workflow.client.AuthServiceClient;
import com.dentistdss.workflow.client.NotificationServiceClient;
import com.dentistdss.workflow.model.*;
import com.dentistdss.workflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for executing individual workflow steps
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStepExecutor {
    
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    
    @Transactional
    public void executeStep(WorkflowExecution execution) {
        log.info("Executing step: {} for workflow instance: {}", 
                execution.getStepName(), execution.getWorkflowInstance().getId());
        
        try {
            // Update step status to running
            execution.setStatus(StepStatus.RUNNING);
            execution.setStartedAt(LocalDateTime.now());
            
            // Set timeout if configured
            if (execution.getStepDefinition().getTimeoutMinutes() != null) {
                execution.setTimeoutAt(LocalDateTime.now().plusMinutes(
                        execution.getStepDefinition().getTimeoutMinutes()));
            }
            
            workflowExecutionRepository.save(execution);
            
            // Execute based on step type
            switch (execution.getStepType()) {
                case AUTOMATED:
                    executeAutomatedStep(execution);
                    break;
                case MANUAL_APPROVAL:
                    executeManualApprovalStep(execution);
                    break;
                case SERVICE_CALL:
                    executeServiceCallStep(execution);
                    break;
                case NOTIFICATION:
                    executeNotificationStep(execution);
                    break;
                case CONDITIONAL:
                    executeConditionalStep(execution);
                    break;
                case WAIT:
                    executeWaitStep(execution);
                    break;
                default:
                    throw new UnsupportedOperationException("Step type not supported: " + execution.getStepType());
            }
            
        } catch (Exception e) {
            log.error("Error executing step: {} - {}", execution.getStepName(), e.getMessage(), e);
            handleStepFailure(execution, e.getMessage());
        }
    }
    
    private void executeAutomatedStep(WorkflowExecution execution) {
        log.info("Executing automated step: {}", execution.getStepName());
        
        // For automated steps, we can implement custom logic based on step configuration
        Map<String, Object> stepConfig = execution.getStepDefinition().getStepConfiguration();
        Map<String, Object> inputData = execution.getWorkflowInstance().getInputData();
        
        // Example: Simple data transformation or validation
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("executedAt", LocalDateTime.now());
        outputData.put("stepName", execution.getStepName());
        
        // Complete the step
        completeStep(execution, outputData);
    }
    
    private void executeManualApprovalStep(WorkflowExecution execution) {
        log.info("Executing manual approval step: {}", execution.getStepName());
        
        // Set step to waiting for approval
        execution.setStatus(StepStatus.WAITING_APPROVAL);
        workflowExecutionRepository.save(execution);
        
        // Send notification to approvers
        sendApprovalNotification(execution);
        
        log.info("Step waiting for approval: {}", execution.getStepName());
    }
    
    private void executeServiceCallStep(WorkflowExecution execution) {
        log.info("Executing service call step: {}", execution.getStepName());
        
        String serviceEndpoint = execution.getStepDefinition().getServiceEndpoint();
        Map<String, Object> inputData = execution.getWorkflowInstance().getInputData();
        
        try {
            Map<String, Object> outputData = new HashMap<>();
            
            // Handle specific service calls based on endpoint
            if (serviceEndpoint != null) {
                if (serviceEndpoint.contains("auth-service")) {
                    outputData = executeAuthServiceCall(execution, inputData);
                } else {
                    // Generic service call handling
                    outputData.put("serviceCall", "completed");
                    outputData.put("endpoint", serviceEndpoint);
                }
            }
            
            completeStep(execution, outputData);
            
        } catch (Exception e) {
            log.error("Service call failed for step: {} - {}", execution.getStepName(), e.getMessage());
            handleStepFailure(execution, "Service call failed: " + e.getMessage());
        }
    }
    
    private void executeNotificationStep(WorkflowExecution execution) {
        log.info("Executing notification step: {}", execution.getStepName());
        
        try {
            String template = execution.getStepDefinition().getNotificationTemplate();
            Map<String, Object> inputData = execution.getWorkflowInstance().getInputData();
            
            // Prepare notification data
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("template", template);
            notificationRequest.put("templateVariables", inputData);
            
            // Send notification
            notificationServiceClient.sendNotificationEmail(notificationRequest);
            
            Map<String, Object> outputData = new HashMap<>();
            outputData.put("notificationSent", true);
            outputData.put("template", template);
            
            completeStep(execution, outputData);
            
        } catch (Exception e) {
            log.error("Notification failed for step: {} - {}", execution.getStepName(), e.getMessage());
            handleStepFailure(execution, "Notification failed: " + e.getMessage());
        }
    }
    
    private void executeConditionalStep(WorkflowExecution execution) {
        log.info("Executing conditional step: {}", execution.getStepName());
        
        String conditionExpression = execution.getStepDefinition().getConditionExpression();
        Map<String, Object> contextData = execution.getWorkflowInstance().getContextData();
        
        // Simple condition evaluation (can be enhanced with expression engine)
        boolean conditionMet = evaluateCondition(conditionExpression, contextData);
        
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("conditionMet", conditionMet);
        outputData.put("expression", conditionExpression);
        
        if (conditionMet) {
            completeStep(execution, outputData);
        } else {
            // Skip step if condition not met
            execution.setStatus(StepStatus.SKIPPED);
            execution.setOutputData(outputData);
            execution.setCompletedAt(LocalDateTime.now());
            workflowExecutionRepository.save(execution);
        }
    }
    
    private void executeWaitStep(WorkflowExecution execution) {
        log.info("Executing wait step: {}", execution.getStepName());
        
        // For wait steps, we set them to waiting and they will be processed by a scheduler
        execution.setStatus(StepStatus.WAITING_APPROVAL);
        workflowExecutionRepository.save(execution);
        
        // In a real implementation, you would schedule a task to complete this step after the wait period
        log.info("Step waiting for scheduled completion: {}", execution.getStepName());
    }
    
    private Map<String, Object> executeAuthServiceCall(WorkflowExecution execution, Map<String, Object> inputData) {
        Map<String, Object> outputData = new HashMap<>();
        
        // Handle user approval workflow specific calls
        if (execution.getStepName().contains("update_user_status")) {
            Long userId = (Long) inputData.get("userId");
            String approvalStatus = (String) inputData.get("approvalStatus");
            
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("approvalStatus", approvalStatus);
            updateRequest.put("approvedBy", inputData.get("approvedBy"));
            updateRequest.put("approvalDate", LocalDateTime.now());
            updateRequest.put("enabled", "APPROVED".equals(approvalStatus));
            
            authServiceClient.updateUserApprovalStatus(userId, updateRequest);
            
            outputData.put("userStatusUpdated", true);
            outputData.put("userId", userId);
            outputData.put("status", approvalStatus);
        }
        
        return outputData;
    }
    
    private void sendApprovalNotification(WorkflowExecution execution) {
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("template", "step_approval_required");
            
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("stepName", execution.getStepName());
            templateVariables.put("workflowName", execution.getWorkflowInstance().getWorkflowDefinition().getDisplayName());
            templateVariables.put("instanceId", execution.getWorkflowInstance().getId());
            templateVariables.put("executionId", execution.getId());
            
            notificationRequest.put("templateVariables", templateVariables);
            
            notificationServiceClient.sendNotificationEmail(notificationRequest);
            
        } catch (Exception e) {
            log.error("Failed to send approval notification for step: {} - {}", 
                    execution.getStepName(), e.getMessage());
        }
    }
    
    private boolean evaluateCondition(String expression, Map<String, Object> contextData) {
        // Simple condition evaluation - can be enhanced with a proper expression engine
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }
        
        // Example: "status == 'APPROVED'"
        if (expression.contains("==")) {
            String[] parts = expression.split("==");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String expectedValue = parts[1].trim().replace("'", "").replace("\"", "");
                Object actualValue = contextData.get(key);
                return expectedValue.equals(String.valueOf(actualValue));
            }
        }
        
        return true; // Default to true if expression cannot be evaluated
    }
    
    private void completeStep(WorkflowExecution execution, Map<String, Object> outputData) {
        execution.setStatus(StepStatus.COMPLETED);
        execution.setOutputData(outputData);
        execution.setCompletedAt(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
        
        log.info("Step completed: {}", execution.getStepName());
    }
    
    private void handleStepFailure(WorkflowExecution execution, String errorMessage) {
        execution.setStatus(StepStatus.FAILED);
        execution.setErrorMessage(errorMessage);
        execution.setCompletedAt(LocalDateTime.now());
        workflowExecutionRepository.save(execution);
        
        log.error("Step failed: {} - {}", execution.getStepName(), errorMessage);
    }
}
