package press.mizhifei.dentist.systemadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.systemadmin.model.RateLimitConfig;

import java.time.LocalDateTime;

/**
 * Response DTO for Rate Limit Configuration operations
 * 
 * Comprehensive data transfer for rate limiting administration responses
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfigResponse {
    
    private Long id;
    private String configName;
    private String serviceName;
    private String endpointPattern;
    private String userRole;
    private Long clinicId;
    private Long maxRequests;
    private Long timeWindowSeconds;
    private RateLimitConfig.RateLimitType limitType;
    private String limitTypeDisplayName;
    private Integer priority;
    private Boolean active;
    private String description;
    private String category;
    private String environment;
    private Integer version;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String summary;
    
    /**
     * Create from entity
     */
    public static RateLimitConfigResponse fromEntity(RateLimitConfig entity) {
        return RateLimitConfigResponse.builder()
                .id(entity.getId())
                .configName(entity.getConfigName())
                .serviceName(entity.getServiceName())
                .endpointPattern(entity.getEndpointPattern())
                .userRole(entity.getUserRole())
                .clinicId(entity.getClinicId())
                .maxRequests(entity.getMaxRequests())
                .timeWindowSeconds(entity.getTimeWindowSeconds())
                .limitType(entity.getLimitType())
                .limitTypeDisplayName(entity.getLimitType().getDisplayName())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .environment(entity.getEnvironment())
                .version(entity.getVersion())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .summary(entity.getSummary())
                .build();
    }
    
    /**
     * Get formatted rate limit description
     */
    public String getFormattedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(maxRequests).append(" ").append(limitType.getDisplayName().toLowerCase());
        sb.append(" per ").append(timeWindowSeconds).append(" seconds");
        
        if (userRole != null) {
            sb.append(" for ").append(userRole);
        }
        
        if (clinicId != null) {
            sb.append(" (Clinic ID: ").append(clinicId).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Check if this is a global configuration
     */
    public boolean isGlobal() {
        return userRole == null && clinicId == null && environment == null;
    }
    
    /**
     * Check if this is a role-specific configuration
     */
    public boolean isRoleSpecific() {
        return userRole != null;
    }
    
    /**
     * Check if this is a clinic-specific configuration
     */
    public boolean isClinicSpecific() {
        return clinicId != null;
    }
    
    /**
     * Check if this is an environment-specific configuration
     */
    public boolean isEnvironmentSpecific() {
        return environment != null;
    }
    
    /**
     * Get configuration scope description
     */
    public String getScopeDescription() {
        if (isGlobal()) {
            return "Global";
        }
        
        StringBuilder scope = new StringBuilder();
        if (isRoleSpecific()) {
            scope.append("Role: ").append(userRole);
        }
        if (isClinicSpecific()) {
            if (scope.length() > 0) scope.append(", ");
            scope.append("Clinic: ").append(clinicId);
        }
        if (isEnvironmentSpecific()) {
            if (scope.length() > 0) scope.append(", ");
            scope.append("Environment: ").append(environment);
        }
        
        return scope.toString();
    }
    
    /**
     * Get priority level description
     */
    public String getPriorityLevel() {
        if (priority == null) return "Normal";
        if (priority >= 80) return "Critical";
        if (priority >= 60) return "High";
        if (priority >= 40) return "Medium";
        if (priority >= 20) return "Low";
        return "Minimal";
    }
    
    /**
     * Calculate requests per minute
     */
    public double getRequestsPerMinute() {
        if (timeWindowSeconds == null || timeWindowSeconds == 0) return 0;
        return (double) maxRequests * 60.0 / timeWindowSeconds;
    }
    
    /**
     * Calculate requests per hour
     */
    public double getRequestsPerHour() {
        if (timeWindowSeconds == null || timeWindowSeconds == 0) return 0;
        return (double) maxRequests * 3600.0 / timeWindowSeconds;
    }
}
