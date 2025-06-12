package com.dentistdss.workflow.controller;

import com.dentistdss.workflow.dto.ApiResponse;
import com.dentistdss.workflow.dto.WorkflowDefinitionRequest;
import com.dentistdss.workflow.model.WorkflowDefinition;
import com.dentistdss.workflow.service.WorkflowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for workflow definition management
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@RestController
@RequestMapping("/workflow/definition")
@RequiredArgsConstructor
@Tag(name = "Workflow Definition", description = "Workflow definition management APIs")
public class WorkflowDefinitionController {
    
    private final WorkflowDefinitionService workflowDefinitionService;
    
    @PostMapping
    @Operation(summary = "Create workflow definition", description = "Create a new workflow definition")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> createWorkflowDefinition(
            @Valid @RequestBody WorkflowDefinitionRequest request,
            @Parameter(description = "User ID creating the workflow") @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        ApiResponse<WorkflowDefinition> response = workflowDefinitionService.createWorkflowDefinition(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get workflow definition", description = "Get workflow definition by ID")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getWorkflowDefinition(
            @Parameter(description = "Workflow definition ID") @PathVariable Long id) {
        
        ApiResponse<WorkflowDefinition> response = workflowDefinitionService.getWorkflowDefinition(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "Get workflow definition by name", description = "Get latest version of workflow definition by name")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getWorkflowDefinitionByName(
            @Parameter(description = "Workflow name") @PathVariable String name) {
        
        ApiResponse<WorkflowDefinition> response = workflowDefinitionService.getWorkflowDefinitionByName(name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/name/{name}/version/{version}")
    @Operation(summary = "Get workflow definition by name and version", description = "Get specific version of workflow definition")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getWorkflowDefinitionByNameAndVersion(
            @Parameter(description = "Workflow name") @PathVariable String name,
            @Parameter(description = "Workflow version") @PathVariable Integer version) {
        
        ApiResponse<WorkflowDefinition> response = workflowDefinitionService.getWorkflowDefinitionByNameAndVersion(name, version);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all active workflow definitions", description = "Get all active workflow definitions")
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> getAllActiveWorkflowDefinitions() {
        ApiResponse<List<WorkflowDefinition>> response = workflowDefinitionService.getAllActiveWorkflowDefinitions();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get workflow definitions by category", description = "Get workflow definitions filtered by category")
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> getWorkflowDefinitionsByCategory(
            @Parameter(description = "Workflow category") @PathVariable String category) {
        
        ApiResponse<List<WorkflowDefinition>> response = workflowDefinitionService.getWorkflowDefinitionsByCategory(category);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update workflow definition", description = "Update an existing workflow definition")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> updateWorkflowDefinition(
            @Parameter(description = "Workflow definition ID") @PathVariable Long id,
            @Valid @RequestBody WorkflowDefinitionRequest request,
            @Parameter(description = "User ID updating the workflow") @RequestHeader(value = "X-User-ID", required = false) Long userId) {
        
        ApiResponse<WorkflowDefinition> response = workflowDefinitionService.updateWorkflowDefinition(id, request, userId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workflow definition", description = "Delete a workflow definition")
    public ResponseEntity<ApiResponse<String>> deleteWorkflowDefinition(
            @Parameter(description = "Workflow definition ID") @PathVariable Long id) {
        
        ApiResponse<String> response = workflowDefinitionService.deleteWorkflowDefinition(id);
        return ResponseEntity.ok(response);
    }
}
