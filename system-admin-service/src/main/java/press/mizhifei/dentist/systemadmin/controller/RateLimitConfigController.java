package press.mizhifei.dentist.systemadmin.controller;

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
import press.mizhifei.dentist.systemadmin.dto.ApiResponse;
import press.mizhifei.dentist.systemadmin.dto.RateLimitConfigRequest;
import press.mizhifei.dentist.systemadmin.dto.RateLimitConfigResponse;
import press.mizhifei.dentist.systemadmin.model.RateLimitConfig;
import press.mizhifei.dentist.systemadmin.service.RateLimitConfigService;

import java.util.List;

/**
 * Rate Limit Configuration Controller
 * 
 * Comprehensive REST API for managing rate limiting configurations
 * with SYSTEM_ADMIN role-based access control
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/api/system-admin/config/rate-limits")
@RequiredArgsConstructor
@Tag(name = "Rate Limit Configuration", description = "System administration endpoints for rate limiting configuration")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class RateLimitConfigController {

    private final RateLimitConfigService rateLimitConfigService;

    /**
     * Get all rate limit configurations
     */
    @GetMapping
    @Operation(summary = "Get all rate limit configurations", 
               description = "Retrieve all active rate limit configurations in the system")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getAllConfigurations() {
        log.debug("Request to get all rate limit configurations");
        
        List<RateLimitConfigResponse> configurations = rateLimitConfigService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configurations, "Rate limit configurations retrieved successfully"));
    }

    /**
     * Get rate limit configuration by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get rate limit configuration by ID", 
               description = "Retrieve a specific rate limit configuration by its ID")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> getConfigurationById(
            @Parameter(description = "Configuration ID") @PathVariable Long id) {
        log.debug("Request to get rate limit configuration with ID: {}", id);
        
        return rateLimitConfigService.getConfigurationById(id)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config, "Configuration retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get rate limit configuration by name
     */
    @GetMapping("/name/{configName}")
    @Operation(summary = "Get rate limit configuration by name", 
               description = "Retrieve a specific rate limit configuration by its name")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> getConfigurationByName(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        log.debug("Request to get rate limit configuration with name: {}", configName);
        
        return rateLimitConfigService.getConfigurationByName(configName)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config, "Configuration retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get rate limit configurations by service
     */
    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Get rate limit configurations by service", 
               description = "Retrieve all rate limit configurations for a specific service")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getConfigurationsByService(
            @Parameter(description = "Service name") @PathVariable String serviceName) {
        log.debug("Request to get rate limit configurations for service: {}", serviceName);
        
        List<RateLimitConfigResponse> configurations = rateLimitConfigService.getConfigurationsByService(serviceName);
        return ResponseEntity.ok(ApiResponse.success(configurations, 
                "Rate limit configurations for service " + serviceName + " retrieved successfully"));
    }

    /**
     * Get rate limit configurations by user role
     */
    @GetMapping("/role/{userRole}")
    @Operation(summary = "Get rate limit configurations by user role", 
               description = "Retrieve all rate limit configurations for a specific user role")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getConfigurationsByRole(
            @Parameter(description = "User role") @PathVariable String userRole) {
        log.debug("Request to get rate limit configurations for role: {}", userRole);
        
        List<RateLimitConfigResponse> configurations = rateLimitConfigService.getConfigurationsByRole(userRole);
        return ResponseEntity.ok(ApiResponse.success(configurations, 
                "Rate limit configurations for role " + userRole + " retrieved successfully"));
    }

    /**
     * Get rate limit configurations by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get rate limit configurations by category", 
               description = "Retrieve all rate limit configurations for a specific category")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getConfigurationsByCategory(
            @Parameter(description = "Configuration category") @PathVariable String category) {
        log.debug("Request to get rate limit configurations for category: {}", category);
        
        List<RateLimitConfigResponse> configurations = rateLimitConfigService.getConfigurationsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(configurations, 
                "Rate limit configurations for category " + category + " retrieved successfully"));
    }

    /**
     * Find best matching configuration
     */
    @GetMapping("/match")
    @Operation(summary = "Find best matching rate limit configuration", 
               description = "Find the best matching rate limit configuration for given criteria")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> findBestMatchingConfiguration(
            @Parameter(description = "Service name") @RequestParam String serviceName,
            @Parameter(description = "User role") @RequestParam(required = false) String userRole,
            @Parameter(description = "Clinic ID") @RequestParam(required = false) Long clinicId,
            @Parameter(description = "Environment") @RequestParam(required = false) String environment) {
        log.debug("Request to find best matching configuration for service: {}, role: {}, clinic: {}, environment: {}", 
                serviceName, userRole, clinicId, environment);
        
        return rateLimitConfigService.findBestMatchingConfiguration(serviceName, userRole, clinicId, environment)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config, "Best matching configuration found")))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No matching configuration found")));
    }

    /**
     * Create new rate limit configuration
     */
    @PostMapping
    @Operation(summary = "Create rate limit configuration", 
               description = "Create a new rate limit configuration")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> createConfiguration(
            @Valid @RequestBody RateLimitConfigRequest request,
            Authentication authentication) {
        log.info("Request to create rate limit configuration: {} by user: {}", 
                request.getConfigName(), authentication.getName());
        
        try {
            RateLimitConfigResponse response = rateLimitConfigService.createConfiguration(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Rate limit configuration created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Update existing rate limit configuration
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update rate limit configuration", 
               description = "Update an existing rate limit configuration")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> updateConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable Long id,
            @Valid @RequestBody RateLimitConfigRequest request,
            Authentication authentication) {
        log.info("Request to update rate limit configuration with ID: {} by user: {}", 
                id, authentication.getName());
        
        try {
            RateLimitConfigResponse response = rateLimitConfigService.updateConfiguration(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response, "Rate limit configuration updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Delete rate limit configuration
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rate limit configuration", 
               description = "Delete an existing rate limit configuration")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable Long id,
            Authentication authentication) {
        log.info("Request to delete rate limit configuration with ID: {} by user: {}", 
                id, authentication.getName());
        
        try {
            rateLimitConfigService.deleteConfiguration(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.successMessage("Rate limit configuration deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Toggle configuration active status
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle rate limit configuration", 
               description = "Activate or deactivate a rate limit configuration")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> toggleConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable Long id,
            @Parameter(description = "Active status") @RequestParam boolean active,
            Authentication authentication) {
        log.info("Request to toggle rate limit configuration with ID: {} to active: {} by user: {}", 
                id, active, authentication.getName());
        
        try {
            RateLimitConfigResponse response = rateLimitConfigService.toggleConfiguration(id, active, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(response, 
                    "Rate limit configuration " + (active ? "activated" : "deactivated") + " successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        }
    }

    /**
     * Get configuration statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get rate limit configuration statistics", 
               description = "Retrieve statistics about rate limit configurations")
    public ResponseEntity<ApiResponse<RateLimitConfigService.ConfigurationStatistics>> getStatistics() {
        log.debug("Request to get rate limit configuration statistics");
        
        RateLimitConfigService.ConfigurationStatistics statistics = rateLimitConfigService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Configuration statistics retrieved successfully"));
    }
}
