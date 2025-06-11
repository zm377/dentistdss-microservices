package com.dentistdss.systemadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.systemadmin.model.ConfigurationRefreshLog;

import java.util.List;

/**
 * Request DTO for Configuration Refresh operations
 * 
 * Comprehensive validation and data transfer for configuration refresh administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationRefreshRequest {
    
    /**
     * Type of refresh operation
     */
    private ConfigurationRefreshLog.RefreshType refreshType;
    
    /**
     * Target service name (for single service refresh)
     */
    @Size(max = 50, message = "Service name must not exceed 50 characters")
    private String serviceName;
    
    /**
     * Target service names (for batch refresh)
     */
    private List<String> serviceNames;
    
    /**
     * Reason for the refresh
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    /**
     * Configuration changes that triggered this refresh
     */
    @Size(max = 2000, message = "Configuration changes description must not exceed 2000 characters")
    private String configurationChanges;
    
    /**
     * Whether to force refresh even if no changes detected
     */
    private Boolean forceRefresh;
    
    /**
     * Timeout for refresh operation in seconds
     */
    private Integer timeoutSeconds;
    
    /**
     * Whether to refresh asynchronously
     */
    private Boolean async;
    
    /**
     * Validate the request based on refresh type
     */
    public boolean isValid() {
        if (refreshType == null) return false;
        
        switch (refreshType) {
            case SINGLE_SERVICE:
                return serviceName != null && !serviceName.trim().isEmpty();
            case BATCH_SERVICES:
                return serviceNames != null && !serviceNames.isEmpty();
            case ALL_SERVICES:
            case RATE_LIMIT_UPDATE:
            case SYSTEM_PARAM_UPDATE:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get target services as comma-separated string
     */
    public String getTargetServicesString() {
        switch (refreshType) {
            case SINGLE_SERVICE:
                return serviceName;
            case BATCH_SERVICES:
                return serviceNames != null ? String.join(",", serviceNames) : "";
            case ALL_SERVICES:
                return "ALL";
            case RATE_LIMIT_UPDATE:
                return "RATE_LIMIT_AFFECTED";
            case SYSTEM_PARAM_UPDATE:
                return "PARAM_AFFECTED";
            default:
                return "";
        }
    }
    
    /**
     * Get total number of target services
     */
    public int getTotalServices() {
        switch (refreshType) {
            case SINGLE_SERVICE:
                return 1;
            case BATCH_SERVICES:
                return serviceNames != null ? serviceNames.size() : 0;
            case ALL_SERVICES:
                return getAllServiceNames().size();
            case RATE_LIMIT_UPDATE:
            case SYSTEM_PARAM_UPDATE:
                return getAffectedServiceNames().size();
            default:
                return 0;
        }
    }
    
    /**
     * Get all known service names in the system
     */
    private List<String> getAllServiceNames() {
        return List.of(
            "api-gateway",
            "auth-service", 
            "clinic-admin-service",
            "appointment-service",
            "clinical-records-service",
            "user-profile-service",
            "genai-service",
            "chat-log-service",
            "reporting-service",
            "system-admin-service",
            "audit-service",
            "notification-service",
            "discovery-server",
            "admin-server",
            "config-server"
        );
    }
    
    /**
     * Get service names that might be affected by configuration changes
     */
    private List<String> getAffectedServiceNames() {
        // For rate limit and system parameter updates, 
        // we typically need to refresh most application services
        return List.of(
            "api-gateway",
            "auth-service",
            "clinic-admin-service", 
            "appointment-service",
            "clinical-records-service",
            "user-profile-service",
            "genai-service",
            "chat-log-service",
            "reporting-service",
            "audit-service",
            "notification-service"
        );
    }
    
    /**
     * Create request for refreshing all services
     */
    public static ConfigurationRefreshRequest refreshAll(String reason) {
        return ConfigurationRefreshRequest.builder()
                .refreshType(ConfigurationRefreshLog.RefreshType.ALL_SERVICES)
                .reason(reason)
                .async(true)
                .timeoutSeconds(300) // 5 minutes
                .build();
    }
    
    /**
     * Create request for refreshing a single service
     */
    public static ConfigurationRefreshRequest refreshService(String serviceName, String reason) {
        return ConfigurationRefreshRequest.builder()
                .refreshType(ConfigurationRefreshLog.RefreshType.SINGLE_SERVICE)
                .serviceName(serviceName)
                .reason(reason)
                .async(false)
                .timeoutSeconds(60) // 1 minute
                .build();
    }
    
    /**
     * Create request for batch refresh
     */
    public static ConfigurationRefreshRequest refreshBatch(List<String> serviceNames, String reason) {
        return ConfigurationRefreshRequest.builder()
                .refreshType(ConfigurationRefreshLog.RefreshType.BATCH_SERVICES)
                .serviceNames(serviceNames)
                .reason(reason)
                .async(true)
                .timeoutSeconds(180) // 3 minutes
                .build();
    }
    
    /**
     * Create request for rate limit configuration update
     */
    public static ConfigurationRefreshRequest refreshForRateLimit(String configChanges) {
        return ConfigurationRefreshRequest.builder()
                .refreshType(ConfigurationRefreshLog.RefreshType.RATE_LIMIT_UPDATE)
                .reason("Rate limit configuration updated")
                .configurationChanges(configChanges)
                .async(true)
                .timeoutSeconds(120) // 2 minutes
                .build();
    }
    
    /**
     * Create request for system parameter update
     */
    public static ConfigurationRefreshRequest refreshForSystemParam(String configChanges) {
        return ConfigurationRefreshRequest.builder()
                .refreshType(ConfigurationRefreshLog.RefreshType.SYSTEM_PARAM_UPDATE)
                .reason("System parameter updated")
                .configurationChanges(configChanges)
                .async(true)
                .timeoutSeconds(120) // 2 minutes
                .build();
    }
}
