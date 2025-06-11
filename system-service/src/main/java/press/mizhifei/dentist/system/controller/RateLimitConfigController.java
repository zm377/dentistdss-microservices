package press.mizhifei.dentist.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.system.dto.ApiResponse;
import press.mizhifei.dentist.system.dto.RateLimitConfigRequest;
import press.mizhifei.dentist.system.dto.RateLimitConfigResponse;
import press.mizhifei.dentist.system.service.RateLimitConfigService;

import java.util.List;
import java.util.Optional;

/**
 * Controller for Rate Limit Configuration Management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/system/rate-limit")
@RequiredArgsConstructor
@Tag(name = "Rate Limit Configuration", description = "Manage rate limiting configurations for microservices")
public class RateLimitConfigController {
    
    private final RateLimitConfigService rateLimitConfigService;
    
    /**
     * Create or update rate limit configuration
     */
    @PostMapping
    @Operation(summary = "Create or update rate limit configuration", 
               description = "Creates a new rate limit configuration or updates an existing one")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> createOrUpdate(
            @Valid @RequestBody RateLimitConfigRequest request) {
        
        log.info("Creating/updating rate limit config: {}", request.getConfigName());
        RateLimitConfigResponse response = rateLimitConfigService.createOrUpdate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all rate limit configurations
     */
    @GetMapping
    @Operation(summary = "Get all rate limit configurations", 
               description = "Retrieves all rate limit configurations")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getAllConfigurations() {
        List<RateLimitConfigResponse> configs = rateLimitConfigService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
    
    /**
     * Get active rate limit configurations
     */
    @GetMapping("/active")
    @Operation(summary = "Get active rate limit configurations", 
               description = "Retrieves only active rate limit configurations")
    public ResponseEntity<ApiResponse<List<RateLimitConfigResponse>>> getActiveConfigurations() {
        List<RateLimitConfigResponse> configs = rateLimitConfigService.getActiveConfigurations();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
    
    /**
     * Get configuration by name
     */
    @GetMapping("/{configName}")
    @Operation(summary = "Get rate limit configuration by name", 
               description = "Retrieves a specific rate limit configuration by its name")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> getByConfigName(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        Optional<RateLimitConfigResponse> config = rateLimitConfigService.getByConfigName(configName);
        
        if (config.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(config.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Find best matching configuration for a request context
     */
    @GetMapping("/match")
    @Operation(summary = "Find matching rate limit configuration", 
               description = "Finds the best matching rate limit configuration for a given context")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> findMatchingConfig(
            @Parameter(description = "Endpoint path") @RequestParam String endpoint,
            @Parameter(description = "User role") @RequestParam(required = false) String userRole,
            @Parameter(description = "Clinic ID") @RequestParam(required = false) Long clinicId) {
        
        Optional<RateLimitConfigResponse> config = rateLimitConfigService.findBestMatchingConfig(
                endpoint, userRole, clinicId);
        
        if (config.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(config.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete configuration
     */
    @DeleteMapping("/{configName}")
    @Operation(summary = "Delete rate limit configuration", 
               description = "Deletes a rate limit configuration by name")
    public ResponseEntity<ApiResponse<String>> deleteConfiguration(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        rateLimitConfigService.deleteConfiguration(configName);
        return ResponseEntity.ok(ApiResponse.success("Configuration deleted successfully"));
    }
    
    /**
     * Toggle configuration active status
     */
    @PatchMapping("/{configName}/toggle")
    @Operation(summary = "Toggle rate limit configuration status", 
               description = "Toggles the active status of a rate limit configuration")
    public ResponseEntity<ApiResponse<RateLimitConfigResponse>> toggleActive(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        RateLimitConfigResponse response = rateLimitConfigService.toggleActive(configName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
