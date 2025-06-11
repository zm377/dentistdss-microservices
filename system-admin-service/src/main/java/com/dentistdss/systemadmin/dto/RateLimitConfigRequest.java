package com.dentistdss.systemadmin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.systemadmin.model.RateLimitConfig;

/**
 * Request DTO for Rate Limit Configuration operations
 * 
 * Comprehensive validation and data transfer for rate limiting administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfigRequest {
    
    @NotBlank(message = "Configuration name is required")
    @Size(max = 100, message = "Configuration name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Configuration name can only contain letters, numbers, underscores, and hyphens")
    private String configName;
    
    @NotBlank(message = "Service name is required")
    @Size(max = 50, message = "Service name must not exceed 50 characters")
    private String serviceName;
    
    @NotBlank(message = "Endpoint pattern is required")
    @Size(max = 200, message = "Endpoint pattern must not exceed 200 characters")
    private String endpointPattern;
    
    @Size(max = 50, message = "User role must not exceed 50 characters")
    @Pattern(regexp = "^(SYSTEM_ADMIN|CLINIC_ADMIN|DENTIST|RECEPTIONIST|PATIENT)$", 
             message = "User role must be one of: SYSTEM_ADMIN, CLINIC_ADMIN, DENTIST, RECEPTIONIST, PATIENT")
    private String userRole;
    
    @Min(value = 1, message = "Clinic ID must be positive")
    private Long clinicId;
    
    @NotNull(message = "Max requests is required")
    @Min(value = 1, message = "Max requests must be positive")
    @Max(value = 1000000, message = "Max requests cannot exceed 1,000,000")
    private Long maxRequests;
    
    @NotNull(message = "Time window is required")
    @Min(value = 1, message = "Time window must be at least 1 second")
    @Max(value = 86400, message = "Time window cannot exceed 24 hours (86400 seconds)")
    private Long timeWindowSeconds;
    
    @NotNull(message = "Limit type is required")
    private RateLimitConfig.RateLimitType limitType;
    
    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority;
    
    private Boolean active;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;
    
    @Size(max = 20, message = "Environment must not exceed 20 characters")
    @Pattern(regexp = "^(dev|docker|prod)$", message = "Environment must be one of: dev, docker, prod")
    private String environment;
    
    /**
     * Validate the request data
     */
    public boolean isValid() {
        if (configName == null || configName.trim().isEmpty()) return false;
        if (serviceName == null || serviceName.trim().isEmpty()) return false;
        if (endpointPattern == null || endpointPattern.trim().isEmpty()) return false;
        if (maxRequests == null || maxRequests <= 0) return false;
        if (timeWindowSeconds == null || timeWindowSeconds <= 0) return false;
        if (limitType == null) return false;
        
        // Validate user role if provided
        if (userRole != null && !userRole.matches("^(SYSTEM_ADMIN|CLINIC_ADMIN|DENTIST|RECEPTIONIST|PATIENT)$")) {
            return false;
        }
        
        // Validate clinic ID if provided
        if (clinicId != null && clinicId <= 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Convert to entity
     */
    public RateLimitConfig toEntity() {
        return RateLimitConfig.builder()
                .configName(configName)
                .serviceName(serviceName)
                .endpointPattern(endpointPattern)
                .userRole(userRole)
                .clinicId(clinicId)
                .maxRequests(maxRequests)
                .timeWindowSeconds(timeWindowSeconds)
                .limitType(limitType)
                .priority(priority != null ? priority : 0)
                .active(active != null ? active : true)
                .description(description)
                .category(category != null ? category : "GENERAL")
                .environment(environment)
                .build();
    }
    
    /**
     * Create from entity
     */
    public static RateLimitConfigRequest fromEntity(RateLimitConfig entity) {
        return RateLimitConfigRequest.builder()
                .configName(entity.getConfigName())
                .serviceName(entity.getServiceName())
                .endpointPattern(entity.getEndpointPattern())
                .userRole(entity.getUserRole())
                .clinicId(entity.getClinicId())
                .maxRequests(entity.getMaxRequests())
                .timeWindowSeconds(entity.getTimeWindowSeconds())
                .limitType(entity.getLimitType())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .environment(entity.getEnvironment())
                .build();
    }
}
