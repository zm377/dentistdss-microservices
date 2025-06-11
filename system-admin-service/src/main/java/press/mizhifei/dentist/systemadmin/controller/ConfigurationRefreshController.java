package press.mizhifei.dentist.systemadmin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.systemadmin.dto.ApiResponse;
import press.mizhifei.dentist.systemadmin.dto.ConfigurationRefreshRequest;
import press.mizhifei.dentist.systemadmin.dto.ConfigurationRefreshResponse;
import press.mizhifei.dentist.systemadmin.model.ConfigurationRefreshLog;
import press.mizhifei.dentist.systemadmin.service.ConfigurationRefreshService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Configuration Refresh Controller
 * 
 * Comprehensive REST API for orchestrating configuration refresh operations
 * across all microservices with SYSTEM_ADMIN role-based access control
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/api/system-admin/config/refresh")
@RequiredArgsConstructor
@Tag(name = "Configuration Refresh", description = "System administration endpoints for configuration refresh orchestration")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class ConfigurationRefreshController {

    private final ConfigurationRefreshService configurationRefreshService;

    /**
     * Refresh all services
     */
    @PostMapping("/all")
    @Operation(summary = "Refresh all services", 
               description = "Trigger configuration refresh for all microservices in the system")
    public ResponseEntity<ApiResponse<String>> refreshAllServices(
            @Parameter(description = "Reason for refresh") @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Request to refresh all services by user: {}", authentication.getName());
        
        String effectiveReason = reason != null ? reason : "Manual refresh of all services";
        
        CompletableFuture<ConfigurationRefreshResponse> future = 
                configurationRefreshService.refreshAllServices(authentication.getName(), effectiveReason);
        
        // Return immediately with refresh ID for async tracking
        return ResponseEntity.accepted()
                .body(ApiResponse.success("ASYNC_OPERATION", 
                        "Configuration refresh initiated for all services. Use the refresh ID to track progress."));
    }

    /**
     * Refresh a single service
     */
    @PostMapping("/service/{serviceName}")
    @Operation(summary = "Refresh single service", 
               description = "Trigger configuration refresh for a specific microservice")
    public ResponseEntity<ApiResponse<ConfigurationRefreshResponse>> refreshSingleService(
            @Parameter(description = "Service name") @PathVariable String serviceName,
            @Parameter(description = "Reason for refresh") @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Request to refresh service: {} by user: {}", serviceName, authentication.getName());
        
        String effectiveReason = reason != null ? reason : "Manual refresh of service: " + serviceName;
        
        try {
            ConfigurationRefreshResponse response = 
                    configurationRefreshService.refreshSingleService(serviceName, authentication.getName(), effectiveReason);
            
            return ResponseEntity.ok(ApiResponse.success(response, 
                    "Configuration refresh completed for service: " + serviceName));
        } catch (Exception e) {
            log.error("Failed to refresh service {}: {}", serviceName, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to refresh service: " + e.getMessage(), "REFRESH_ERROR"));
        }
    }

    /**
     * Refresh multiple services (batch)
     */
    @PostMapping("/batch")
    @Operation(summary = "Refresh multiple services", 
               description = "Trigger configuration refresh for multiple specified microservices")
    public ResponseEntity<ApiResponse<String>> refreshBatchServices(
            @Parameter(description = "List of service names") @RequestBody List<String> serviceNames,
            @Parameter(description = "Reason for refresh") @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Request to refresh batch services: {} by user: {}", serviceNames, authentication.getName());
        
        if (serviceNames == null || serviceNames.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Service names list cannot be empty", "VALIDATION_ERROR"));
        }
        
        String effectiveReason = reason != null ? reason : "Manual batch refresh of services: " + String.join(", ", serviceNames);
        
        CompletableFuture<ConfigurationRefreshResponse> future = 
                configurationRefreshService.refreshBatchServices(serviceNames, authentication.getName(), effectiveReason);
        
        // Return immediately with refresh ID for async tracking
        return ResponseEntity.accepted()
                .body(ApiResponse.success("ASYNC_OPERATION", 
                        "Configuration refresh initiated for batch services. Use the refresh ID to track progress."));
    }

    /**
     * Get refresh operation status
     */
    @GetMapping("/status/{refreshId}")
    @Operation(summary = "Get refresh operation status", 
               description = "Retrieve the status of a configuration refresh operation")
    public ResponseEntity<ApiResponse<ConfigurationRefreshResponse>> getRefreshStatus(
            @Parameter(description = "Refresh operation ID") @PathVariable String refreshId) {
        log.debug("Request to get refresh status for ID: {}", refreshId);
        
        return configurationRefreshService.getRefreshStatus(refreshId)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Refresh status retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get recent refresh operations
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent refresh operations", 
               description = "Retrieve recent configuration refresh operations")
    public ResponseEntity<ApiResponse<List<ConfigurationRefreshResponse>>> getRecentRefreshOperations(
            @Parameter(description = "Number of hours to look back") @RequestParam(defaultValue = "24") int hours) {
        log.debug("Request to get recent refresh operations from last {} hours", hours);
        
        List<ConfigurationRefreshResponse> operations = configurationRefreshService.getRecentRefreshOperations(hours);
        return ResponseEntity.ok(ApiResponse.success(operations, 
                "Recent refresh operations retrieved successfully"));
    }

    /**
     * Get refresh operations by status
     */
    @GetMapping("/status")
    @Operation(summary = "Get refresh operations by status", 
               description = "Retrieve configuration refresh operations filtered by status")
    public ResponseEntity<ApiResponse<List<ConfigurationRefreshResponse>>> getRefreshOperationsByStatus(
            @Parameter(description = "Refresh status") @RequestParam ConfigurationRefreshLog.RefreshStatus status) {
        log.debug("Request to get refresh operations with status: {}", status);
        
        List<ConfigurationRefreshResponse> operations = configurationRefreshService.getRefreshOperationsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(operations, 
                "Refresh operations with status " + status + " retrieved successfully"));
    }

    /**
     * Get failed refresh operations
     */
    @GetMapping("/failed")
    @Operation(summary = "Get failed refresh operations", 
               description = "Retrieve all failed configuration refresh operations")
    public ResponseEntity<ApiResponse<List<ConfigurationRefreshResponse>>> getFailedRefreshOperations() {
        log.debug("Request to get failed refresh operations");
        
        List<ConfigurationRefreshResponse> operations = configurationRefreshService.getRefreshOperationsByStatus(
                ConfigurationRefreshLog.RefreshStatus.FAILED);
        return ResponseEntity.ok(ApiResponse.success(operations, "Failed refresh operations retrieved successfully"));
    }

    /**
     * Get successful refresh operations
     */
    @GetMapping("/successful")
    @Operation(summary = "Get successful refresh operations", 
               description = "Retrieve all successful configuration refresh operations")
    public ResponseEntity<ApiResponse<List<ConfigurationRefreshResponse>>> getSuccessfulRefreshOperations() {
        log.debug("Request to get successful refresh operations");
        
        List<ConfigurationRefreshResponse> operations = configurationRefreshService.getRefreshOperationsByStatus(
                ConfigurationRefreshLog.RefreshStatus.COMPLETED);
        return ResponseEntity.ok(ApiResponse.success(operations, "Successful refresh operations retrieved successfully"));
    }

    /**
     * Get in-progress refresh operations
     */
    @GetMapping("/in-progress")
    @Operation(summary = "Get in-progress refresh operations", 
               description = "Retrieve all currently in-progress configuration refresh operations")
    public ResponseEntity<ApiResponse<List<ConfigurationRefreshResponse>>> getInProgressRefreshOperations() {
        log.debug("Request to get in-progress refresh operations");
        
        List<ConfigurationRefreshResponse> operations = configurationRefreshService.getRefreshOperationsByStatus(
                ConfigurationRefreshLog.RefreshStatus.IN_PROGRESS);
        return ResponseEntity.ok(ApiResponse.success(operations, "In-progress refresh operations retrieved successfully"));
    }

    /**
     * Emergency refresh all services
     */
    @PostMapping("/emergency")
    @Operation(summary = "Emergency refresh all services", 
               description = "Trigger emergency configuration refresh for all services with high priority")
    public ResponseEntity<ApiResponse<String>> emergencyRefreshAllServices(
            @Parameter(description = "Emergency reason") @RequestParam String emergencyReason,
            Authentication authentication) {
        log.warn("Emergency refresh request by user: {} - Reason: {}", authentication.getName(), emergencyReason);
        
        String reason = "EMERGENCY: " + emergencyReason;
        
        CompletableFuture<ConfigurationRefreshResponse> future = 
                configurationRefreshService.refreshAllServices(authentication.getName(), reason);
        
        return ResponseEntity.accepted()
                .body(ApiResponse.success("EMERGENCY_REFRESH_INITIATED", 
                        "Emergency configuration refresh initiated for all services."));
    }
}
