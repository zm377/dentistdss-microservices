package com.dentistdss.workflow.service;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.StepApprovalRequest;
import com.dentistdss.workflow.dto.WorkflowInstanceRequest;
import com.dentistdss.workflow.dto.WorkflowInstanceResponse;
import com.dentistdss.workflow.model.*;
import com.dentistdss.workflow.repository.WorkflowDefinitionRepository;
import com.dentistdss.workflow.repository.WorkflowExecutionRepository;
import com.dentistdss.workflow.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for workflow execution and orchestration
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {
    
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowStepExecutor workflowStepExecutor;
    
    @Transactional
    public ApiResponse<WorkflowInstanceResponse> startWorkflow(WorkflowInstanceRequest request, Long startedBy) {
        log.info("Starting workflow: {} for entity: {}:{}", request.getWorkflowName(), 
                request.getEntityType(), request.getEntityId());
        
        // Find workflow definition
        Optional<WorkflowDefinition> workflowDefOpt;
        if (request.getWorkflowVersion() != null) {
            workflowDefOpt = workflowDefinitionRepository.findByNameAndVersion(
                    request.getWorkflowName(), request.getWorkflowVersion());
        } else {
            workflowDefOpt = workflowDefinitionRepository.findLatestVersionByName(request.getWorkflowName());
        }
        
        if (workflowDefOpt.isEmpty()) {
            return ApiResponse.error("Workflow definition not found: " + request.getWorkflowName());
        }
        
        WorkflowDefinition workflowDefinition = workflowDefOpt.get();
        
        if (!workflowDefinition.getIsActive()) {
            return ApiResponse.error("Workflow definition is not active: " + request.getWorkflowName());
        }
        
        // Check for existing workflow instance with same business key
        if (request.getBusinessKey() != null) {
            Optional<WorkflowInstance> existingInstance = workflowInstanceRepository.findByBusinessKey(request.getBusinessKey());
            if (existingInstance.isPresent() && 
                existingInstance.get().getStatus() == WorkflowStatus.RUNNING) {
                return ApiResponse.error("Workflow instance with business key already running: " + request.getBusinessKey());
            }
        }
        
        // Create workflow instance
        WorkflowInstance workflowInstance = WorkflowInstance.builder()
                .workflowDefinition(workflowDefinition)
                .instanceName(request.getInstanceName())
                .businessKey(request.getBusinessKey())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .priority(request.getPriority())
                .inputData(request.getInputData())
                .contextData(request.getContextData())
                .status(WorkflowStatus.CREATED)
                .startedBy(startedBy)
                .build();
        
        // Set timeout if configured
        if (workflowDefinition.getTimeoutMinutes() != null) {
            workflowInstance.setTimeoutAt(LocalDateTime.now().plusMinutes(workflowDefinition.getTimeoutMinutes()));
        }
        
        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);
        
        // Create execution records for all steps
        createExecutionRecords(savedInstance, workflowDefinition);
        
        // Start workflow if auto-start is enabled
        if (request.getAutoStart()) {
            startWorkflowExecution(savedInstance.getId());
        }
        
        log.info("Created workflow instance: {} for workflow: {}", savedInstance.getId(), request.getWorkflowName());
        
        return ApiResponse.success(toWorkflowInstanceResponse(savedInstance), "Workflow started successfully");
    }
    
    @Transactional
    public ApiResponse<String> startWorkflowExecution(Long instanceId) {
        log.info("Starting workflow execution for instance: {}", instanceId);
        
        Optional<WorkflowInstance> instanceOpt = workflowInstanceRepository.findById(instanceId);
        if (instanceOpt.isEmpty()) {
            return ApiResponse.error("Workflow instance not found");
        }
        
        WorkflowInstance instance = instanceOpt.get();
        
        if (instance.getStatus() != WorkflowStatus.CREATED && instance.getStatus() != WorkflowStatus.WAITING) {
            return ApiResponse.error("Workflow instance cannot be started in current status: " + instance.getStatus());
        }
        
        // Update instance status
        instance.setStatus(WorkflowStatus.RUNNING);
        instance.setStartedAt(LocalDateTime.now());
        instance.setCurrentStepOrder(1);
        workflowInstanceRepository.save(instance);
        
        // Execute first step
        executeNextStep(instance);
        
        return ApiResponse.successMessage("Workflow execution started");
    }
    
    @Transactional
    public ApiResponse<String> approveStep(Long executionId, StepApprovalRequest request, Long approvedBy) {
        log.info("Processing step approval for execution: {}", executionId);
        
        Optional<WorkflowExecution> executionOpt = workflowExecutionRepository.findById(executionId);
        if (executionOpt.isEmpty()) {
            return ApiResponse.error("Workflow execution not found");
        }
        
        WorkflowExecution execution = executionOpt.get();
        
        if (execution.getStatus() != StepStatus.WAITING_APPROVAL) {
            return ApiResponse.error("Step is not waiting for approval");
        }
        
        // Update execution with approval decision
        execution.setApprovedBy(approvedBy);
        execution.setApprovalNotes(request.getApprovalNotes());
        execution.setCompletedAt(LocalDateTime.now());
        
        if (request.getOutputData() != null) {
            execution.setOutputData(request.getOutputData());
        }
        
        if (request.getApproved()) {
            execution.setStatus(StepStatus.COMPLETED);
            log.info("Step approved for execution: {}", executionId);
        } else {
            execution.setStatus(StepStatus.FAILED);
            execution.setErrorMessage("Step rejected: " + request.getApprovalNotes());
            log.info("Step rejected for execution: {}", executionId);
        }
        
        workflowExecutionRepository.save(execution);
        
        // Continue workflow execution
        if (request.getApproved()) {
            executeNextStep(execution.getWorkflowInstance());
        } else {
            // Handle workflow failure
            handleWorkflowFailure(execution.getWorkflowInstance(), "Step rejected: " + request.getApprovalNotes());
        }
        
        return ApiResponse.successMessage(request.getApproved() ? "Step approved successfully" : "Step rejected");
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<WorkflowInstanceResponse> getWorkflowInstance(Long instanceId) {
        Optional<WorkflowInstance> instanceOpt = workflowInstanceRepository.findById(instanceId);
        if (instanceOpt.isEmpty()) {
            return ApiResponse.error("Workflow instance not found");
        }
        
        return ApiResponse.success(toWorkflowInstanceResponse(instanceOpt.get()));
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowInstanceResponse>> getWorkflowInstancesByStatus(WorkflowStatus status) {
        List<WorkflowInstance> instances = workflowInstanceRepository.findByStatus(status);
        List<WorkflowInstanceResponse> responses = instances.stream()
                .map(this::toWorkflowInstanceResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses);
    }
    
    private void createExecutionRecords(WorkflowInstance instance, WorkflowDefinition definition) {
        List<WorkflowExecution> executions = definition.getSteps().stream()
                .map(stepDef -> WorkflowExecution.builder()
                        .workflowInstance(instance)
                        .stepDefinition(stepDef)
                        .stepName(stepDef.getStepName())
                        .stepOrder(stepDef.getStepOrder())
                        .stepType(stepDef.getStepType())
                        .status(StepStatus.PENDING)
                        .build())
                .collect(Collectors.toList());
        
        workflowExecutionRepository.saveAll(executions);
        log.info("Created {} execution records for workflow instance: {}", executions.size(), instance.getId());
    }
    
    private void executeNextStep(WorkflowInstance instance) {
        Optional<WorkflowExecution> nextStepOpt = workflowExecutionRepository.findNextPendingStep(instance.getId());
        
        if (nextStepOpt.isEmpty()) {
            // No more steps, complete workflow
            completeWorkflow(instance);
            return;
        }
        
        WorkflowExecution nextStep = nextStepOpt.get();
        
        // Update instance current step
        instance.setCurrentStepOrder(nextStep.getStepOrder());
        workflowInstanceRepository.save(instance);
        
        // Execute the step
        workflowStepExecutor.executeStep(nextStep);
    }
    
    private void completeWorkflow(WorkflowInstance instance) {
        instance.setStatus(WorkflowStatus.COMPLETED);
        instance.setCompletedAt(LocalDateTime.now());
        workflowInstanceRepository.save(instance);
        
        log.info("Workflow instance completed: {}", instance.getId());
    }
    
    private void handleWorkflowFailure(WorkflowInstance instance, String errorMessage) {
        instance.setStatus(WorkflowStatus.FAILED);
        instance.setErrorMessage(errorMessage);
        workflowInstanceRepository.save(instance);
        
        log.error("Workflow instance failed: {} - {}", instance.getId(), errorMessage);
    }
    
    private WorkflowInstanceResponse toWorkflowInstanceResponse(WorkflowInstance instance) {
        return WorkflowInstanceResponse.builder()
                .id(instance.getId())
                .workflowName(instance.getWorkflowDefinition().getName())
                .workflowDisplayName(instance.getWorkflowDefinition().getDisplayName())
                .workflowVersion(instance.getWorkflowDefinition().getVersion())
                .instanceName(instance.getInstanceName())
                .status(instance.getStatus())
                .businessKey(instance.getBusinessKey())
                .entityType(instance.getEntityType())
                .entityId(instance.getEntityId())
                .priority(instance.getPriority())
                .inputData(instance.getInputData())
                .outputData(instance.getOutputData())
                .contextData(instance.getContextData())
                .currentStepOrder(instance.getCurrentStepOrder())
                .errorMessage(instance.getErrorMessage())
                .retryCount(instance.getRetryCount())
                .startedBy(instance.getStartedBy())
                .startedAt(instance.getStartedAt())
                .completedAt(instance.getCompletedAt())
                .timeoutAt(instance.getTimeoutAt())
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();
    }
}
