package com.dentistdss.workflow.service;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowDefinitionRequest;
import com.dentistdss.workflow.model.WorkflowDefinition;
import com.dentistdss.workflow.model.WorkflowStepDefinition;
import com.dentistdss.workflow.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing workflow definitions
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionService {
    
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    
    @Transactional
    public ApiResponse<WorkflowDefinition> createWorkflowDefinition(WorkflowDefinitionRequest request, Long createdBy) {
        log.info("Creating workflow definition: {}", request.getName());
        
        // Check if workflow name already exists
        if (workflowDefinitionRepository.existsByNameAndVersion(request.getName(), request.getVersion())) {
            return ApiResponse.error("Workflow with name '" + request.getName() + 
                    "' and version " + request.getVersion() + " already exists");
        }
        
        // Create workflow definition
        WorkflowDefinition workflowDefinition = WorkflowDefinition.builder()
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .version(request.getVersion())
                .category(request.getCategory())
                .isActive(request.getIsActive())
                .isSystemWorkflow(request.getIsSystemWorkflow())
                .timeoutMinutes(request.getTimeoutMinutes())
                .maxRetryAttempts(request.getMaxRetryAttempts())
                .autoStart(request.getAutoStart())
                .requiresApproval(request.getRequiresApproval())
                .configuration(request.getConfiguration())
                .inputSchema(request.getInputSchema())
                .outputSchema(request.getOutputSchema())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();
        
        // Create step definitions
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            List<WorkflowStepDefinition> stepDefinitions = request.getSteps().stream()
                    .map(stepRequest -> WorkflowStepDefinition.builder()
                            .workflowDefinition(workflowDefinition)
                            .stepName(stepRequest.getStepName())
                            .stepOrder(stepRequest.getStepOrder())
                            .stepType(stepRequest.getStepType())
                            .description(stepRequest.getDescription())
                            .isRequired(stepRequest.getIsRequired())
                            .isParallel(stepRequest.getIsParallel())
                            .timeoutMinutes(stepRequest.getTimeoutMinutes())
                            .retryAttempts(stepRequest.getRetryAttempts())
                            .conditionExpression(stepRequest.getConditionExpression())
                            .approvalRoles(stepRequest.getApprovalRoles())
                            .serviceEndpoint(stepRequest.getServiceEndpoint())
                            .notificationTemplate(stepRequest.getNotificationTemplate())
                            .stepConfiguration(stepRequest.getStepConfiguration())
                            .inputMapping(stepRequest.getInputMapping())
                            .outputMapping(stepRequest.getOutputMapping())
                            .build())
                    .collect(Collectors.toList());
            
            workflowDefinition.setSteps(stepDefinitions);
        }
        
        WorkflowDefinition saved = workflowDefinitionRepository.save(workflowDefinition);
        log.info("Created workflow definition with ID: {}", saved.getId());
        
        return ApiResponse.success(saved, "Workflow definition created successfully");
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<WorkflowDefinition> getWorkflowDefinition(Long id) {
        Optional<WorkflowDefinition> workflowDefinition = workflowDefinitionRepository.findById(id);
        
        if (workflowDefinition.isEmpty()) {
            return ApiResponse.error("Workflow definition not found");
        }
        
        return ApiResponse.success(workflowDefinition.get());
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<WorkflowDefinition> getWorkflowDefinitionByName(String name) {
        Optional<WorkflowDefinition> workflowDefinition = workflowDefinitionRepository.findLatestVersionByName(name);
        
        if (workflowDefinition.isEmpty()) {
            return ApiResponse.error("Workflow definition not found: " + name);
        }
        
        return ApiResponse.success(workflowDefinition.get());
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<WorkflowDefinition> getWorkflowDefinitionByNameAndVersion(String name, Integer version) {
        Optional<WorkflowDefinition> workflowDefinition = workflowDefinitionRepository.findByNameAndVersion(name, version);
        
        if (workflowDefinition.isEmpty()) {
            return ApiResponse.error("Workflow definition not found: " + name + " v" + version);
        }
        
        return ApiResponse.success(workflowDefinition.get());
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowDefinition>> getAllActiveWorkflowDefinitions() {
        List<WorkflowDefinition> definitions = workflowDefinitionRepository.findByIsActiveTrue();
        return ApiResponse.success(definitions);
    }
    
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowDefinition>> getWorkflowDefinitionsByCategory(String category) {
        List<WorkflowDefinition> definitions = workflowDefinitionRepository.findByCategoryAndIsActiveTrue(category);
        return ApiResponse.success(definitions);
    }
    
    @Transactional
    public ApiResponse<WorkflowDefinition> updateWorkflowDefinition(Long id, WorkflowDefinitionRequest request, Long updatedBy) {
        log.info("Updating workflow definition: {}", id);
        
        Optional<WorkflowDefinition> existingOpt = workflowDefinitionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return ApiResponse.error("Workflow definition not found");
        }
        
        WorkflowDefinition existing = existingOpt.get();
        
        // Update fields
        existing.setDisplayName(request.getDisplayName());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        existing.setIsActive(request.getIsActive());
        existing.setTimeoutMinutes(request.getTimeoutMinutes());
        existing.setMaxRetryAttempts(request.getMaxRetryAttempts());
        existing.setAutoStart(request.getAutoStart());
        existing.setRequiresApproval(request.getRequiresApproval());
        existing.setConfiguration(request.getConfiguration());
        existing.setInputSchema(request.getInputSchema());
        existing.setOutputSchema(request.getOutputSchema());
        existing.setUpdatedBy(updatedBy);
        
        WorkflowDefinition saved = workflowDefinitionRepository.save(existing);
        log.info("Updated workflow definition: {}", saved.getId());
        
        return ApiResponse.success(saved, "Workflow definition updated successfully");
    }
    
    @Transactional
    public ApiResponse<String> deleteWorkflowDefinition(Long id) {
        log.info("Deleting workflow definition: {}", id);
        
        if (!workflowDefinitionRepository.existsById(id)) {
            return ApiResponse.error("Workflow definition not found");
        }
        
        workflowDefinitionRepository.deleteById(id);
        log.info("Deleted workflow definition: {}", id);
        
        return ApiResponse.successMessage("Workflow definition deleted successfully");
    }
}
