package com.dentistdss.systemadmin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.systemadmin.dto.ApiResponse;
import com.dentistdss.systemadmin.dto.SystemParameterRequest;
import com.dentistdss.systemadmin.dto.SystemParameterResponse;
import com.dentistdss.systemadmin.model.SystemParameter;
import com.dentistdss.systemadmin.service.SystemParameterService;

import java.util.List;
import java.util.Map;

/**
 * System Parameter Controller
 * 
 * Comprehensive REST API for managing system parameters
 * with SYSTEM_ADMIN role-based access control
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/api/system-admin/config/system-params")
@RequiredArgsConstructor
@Tag(name = "System Parameter Configuration", description = "System administration endpoints for system parameter configuration")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemParameterController {

    private final SystemParameterService systemParameterService;

    /**
     * Get all system parameters
     */
    @GetMapping
    @Operation(summary = "Get all system parameters", 
               description = "Retrieve all active system parameters in the system")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> getAllParameters() {
        log.debug("Request to get all system parameters");
        
        List<SystemParameterResponse> parameters = systemParameterService.getAllParameters();
        return ResponseEntity.ok(ApiResponse.success(parameters, "System parameters retrieved successfully"));
    }

    /**
     * Get system parameter by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get system parameter by ID", 
               description = "Retrieve a specific system parameter by its ID")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> getParameterById(
            @Parameter(description = "Parameter ID") @PathVariable Long id) {
        log.debug("Request to get system parameter with ID: {}", id);
        
        return systemParameterService.getParameterById(id)
                .map(param -> ResponseEntity.ok(ApiResponse.success(param, "Parameter retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get system parameter by key
     */
    @GetMapping("/key/{parameterKey}")
    @Operation(summary = "Get system parameter by key", 
               description = "Retrieve a specific system parameter by its key")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> getParameterByKey(
            @Parameter(description = "Parameter key") @PathVariable String parameterKey) {
        log.debug("Request to get system parameter with key: {}", parameterKey);
        
        return systemParameterService.getParameterByKey(parameterKey)
                .map(param -> ResponseEntity.ok(ApiResponse.success(param, "Parameter retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get system parameters by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get system parameters by category", 
               description = "Retrieve all system parameters for a specific category")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> getParametersByCategory(
            @Parameter(description = "Parameter category") @PathVariable SystemParameter.ParameterCategory category) {
        log.debug("Request to get system parameters for category: {}", category);
        
        List<SystemParameterResponse> parameters = systemParameterService.getParametersByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(parameters, 
                "System parameters for category " + category + " retrieved successfully"));
    }

    /**
     * Get system parameters by service
     */
    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get system parameters by service", 
               description = "Retrieve all system parameters for a specific service")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> getParametersByService(
            @Parameter(description = "Service name") @PathVariable String serviceName) {
        log.debug("Request to get system parameters for service: {}", serviceName);
        
        List<SystemParameterResponse> parameters = systemParameterService.getParametersByService(serviceName);
        return ResponseEntity.ok(ApiResponse.success(parameters, 
                "System parameters for service " + serviceName + " retrieved successfully"));
    }

    /**
     * Get global system parameters
     */
    @GetMapping("/global")
    @Operation(summary = "Get global system parameters", 
               description = "Retrieve all global system parameters (not service-specific)")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> getGlobalParameters() {
        log.debug("Request to get global system parameters");
        
        List<SystemParameterResponse> parameters = systemParameterService.getGlobalParameters();
        return ResponseEntity.ok(ApiResponse.success(parameters, "Global system parameters retrieved successfully"));
    }

    /**
     * Get parameters for service and environment
     */
    @GetMapping("/service/{serviceName}/environment/{environment}")
    @Operation(summary = "Get parameters for service and environment", 
               description = "Retrieve system parameters for a specific service and environment")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> getParametersForServiceAndEnvironment(
            @Parameter(description = "Service name") @PathVariable String serviceName,
            @Parameter(description = "Environment") @PathVariable String environment) {
        log.debug("Request to get system parameters for service: {} and environment: {}", serviceName, environment);
        
        List<SystemParameterResponse> parameters = systemParameterService.getParametersForServiceAndEnvironment(serviceName, environment);
        return ResponseEntity.ok(ApiResponse.success(parameters, 
                "System parameters for service " + serviceName + " and environment " + environment + " retrieved successfully"));
    }

    /**
     * Search system parameters
     */
    @GetMapping("/search")
    @Operation(summary = "Search system parameters", 
               description = "Search system parameters by key or name")
    public ResponseEntity<ApiResponse<List<SystemParameterResponse>>> searchParameters(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        log.debug("Request to search system parameters with term: {}", searchTerm);
        
        List<SystemParameterResponse> parameters = systemParameterService.searchParameters(searchTerm);
        return ResponseEntity.ok(ApiResponse.success(parameters, "Search results retrieved successfully"));
    }

    /**
     * Get parameters as key-value map
     */
    @GetMapping("/map")
    @Operation(summary = "Get parameters as key-value map", 
               description = "Retrieve system parameters as a key-value map for a service and environment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getParametersAsMap(
            @Parameter(description = "Service name") @RequestParam(required = false) String serviceName,
            @Parameter(description = "Environment") @RequestParam(required = false) String environment) {
        log.debug("Request to get system parameters as map for service: {} and environment: {}", serviceName, environment);
        
        Map<String, Object> parametersMap = systemParameterService.getParametersAsMap(serviceName, environment);
        return ResponseEntity.ok(ApiResponse.success(parametersMap, "Parameters map retrieved successfully"));
    }

    /**
     * Create new system parameter
     */
    @PostMapping
    @Operation(summary = "Create system parameter", 
               description = "Create a new system parameter")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> createParameter(
            @Valid @RequestBody SystemParameterRequest request,
            Authentication authentication) {
        log.info("Request to create system parameter: {} by user: {}", 
                request.getParameterKey(), authentication.getName());
        
        try {
            SystemParameterResponse response = systemParameterService.createParameter(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "System parameter created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Update existing system parameter
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update system parameter", 
               description = "Update an existing system parameter")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> updateParameter(
            @Parameter(description = "Parameter ID") @PathVariable Long id,
            @Valid @RequestBody SystemParameterRequest request,
            Authentication authentication) {
        log.info("Request to update system parameter with ID: {} by user: {}", 
                id, authentication.getName());
        
        try {
            SystemParameterResponse response = systemParameterService.updateParameter(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "System parameter updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Update parameter value only
     */
    @PatchMapping("/key/{parameterKey}/value")
    @Operation(summary = "Update parameter value", 
               description = "Update only the value of a system parameter")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> updateParameterValue(
            @Parameter(description = "Parameter key") @PathVariable String parameterKey,
            @Parameter(description = "New value") @RequestParam String value,
            Authentication authentication) {
        log.info("Request to update parameter value for key: {} by user: {}", 
                parameterKey, authentication.getName());
        
        try {
            SystemParameterResponse response = systemParameterService.updateParameterValue(parameterKey, value, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Parameter value updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Delete system parameter
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete system parameter", 
               description = "Delete an existing system parameter")
    public ResponseEntity<ApiResponse<Void>> deleteParameter(
            @Parameter(description = "Parameter ID") @PathVariable Long id,
            Authentication authentication) {
        log.info("Request to delete system parameter with ID: {} by user: {}", 
                id, authentication.getName());
        
        try {
            systemParameterService.deleteParameter(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.successMessage("System parameter deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Toggle parameter active status
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle system parameter", 
               description = "Activate or deactivate a system parameter")
    public ResponseEntity<ApiResponse<SystemParameterResponse>> toggleParameter(
            @Parameter(description = "Parameter ID") @PathVariable Long id,
            @Parameter(description = "Active status") @RequestParam boolean active,
            Authentication authentication) {
        log.info("Request to toggle system parameter with ID: {} to active: {} by user: {}", 
                id, active, authentication.getName());
        
        try {
            SystemParameterResponse response = systemParameterService.toggleParameter(id, active, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response, 
                    "System parameter " + (active ? "activated" : "deactivated") + " successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Get parameter statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get system parameter statistics", 
               description = "Retrieve statistics about system parameters")
    public ResponseEntity<ApiResponse<SystemParameterService.ParameterStatistics>> getStatistics() {
        log.debug("Request to get system parameter statistics");
        
        SystemParameterService.ParameterStatistics statistics = systemParameterService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Parameter statistics retrieved successfully"));
    }
}
