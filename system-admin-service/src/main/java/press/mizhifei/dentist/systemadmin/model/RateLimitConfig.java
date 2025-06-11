package press.mizhifei.dentist.systemadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Enhanced Rate Limiting Configuration Entity
 * 
 * Comprehensive rate limiting configuration for system administration
 * with support for dynamic updates and multi-dimensional limiting
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rate_limit_configs", indexes = {
    @Index(name = "idx_service_endpoint", columnList = "serviceName, endpointPattern"),
    @Index(name = "idx_user_role", columnList = "userRole"),
    @Index(name = "idx_clinic_id", columnList = "clinicId"),
    @Index(name = "idx_active_priority", columnList = "active, priority")
})
public class RateLimitConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique configuration identifier
     */
    @NotBlank(message = "Configuration name is required")
    @Column(unique = true, nullable = false, length = 100)
    private String configName;
    
    /**
     * Target service name (e.g., "genai-service", "reporting-service")
     */
    @NotBlank(message = "Service name is required")
    @Column(nullable = false, length = 50)
    private String serviceName;
    
    /**
     * Endpoint pattern (e.g., "/api/genai/**", "/api/reports/**")
     */
    @NotBlank(message = "Endpoint pattern is required")
    @Column(nullable = false, length = 200)
    private String endpointPattern;
    
    /**
     * User role this limit applies to (null for all users)
     * Values: SYSTEM_ADMIN, CLINIC_ADMIN, DENTIST, RECEPTIONIST, PATIENT
     */
    @Column(length = 50)
    private String userRole;
    
    /**
     * Clinic ID this limit applies to (null for all clinics)
     */
    private Long clinicId;
    
    /**
     * Maximum number of requests/tokens allowed
     */
    @NotNull(message = "Max requests is required")
    @Min(value = 1, message = "Max requests must be positive")
    @Column(nullable = false)
    private Long maxRequests;
    
    /**
     * Time window in seconds
     */
    @NotNull(message = "Time window is required")
    @Min(value = 1, message = "Time window must be positive")
    @Column(nullable = false)
    private Long timeWindowSeconds;
    
    /**
     * Rate limit type: REQUEST_COUNT, TOKEN_COUNT, BANDWIDTH
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RateLimitType limitType;
    
    /**
     * Priority for rule matching (higher number = higher priority)
     */
    @Builder.Default
    private Integer priority = 0;
    
    /**
     * Whether this configuration is active
     */
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Description of this rate limit rule
     */
    @Column(length = 500)
    private String description;
    
    /**
     * Configuration category for grouping
     */
    @Builder.Default
    @Column(length = 50)
    private String category = "GENERAL";
    
    /**
     * Environment this config applies to (null for all environments)
     */
    @Column(length = 20)
    private String environment;
    
    /**
     * Version for configuration tracking
     */
    @Builder.Default
    private Integer version = 1;
    
    /**
     * User who created this configuration
     */
    @Column(length = 100)
    private String createdBy;
    
    /**
     * User who last updated this configuration
     */
    @Column(length = 100)
    private String updatedBy;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
    }
    
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (version != null) {
            version++;
        }
    }
    
    /**
     * Enhanced rate limit types
     */
    public enum RateLimitType {
        /**
         * Limit by number of requests
         */
        REQUEST_COUNT("Request Count"),
        
        /**
         * Limit by token consumption (for AI services)
         */
        TOKEN_COUNT("Token Count"),
        
        /**
         * Limit by bandwidth/data size
         */
        BANDWIDTH("Bandwidth"),
        
        /**
         * Limit by concurrent connections
         */
        CONCURRENT_REQUESTS("Concurrent Requests");
        
        private final String displayName;
        
        RateLimitType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Configuration categories
     */
    public enum ConfigCategory {
        GENERAL("General"),
        AI_SERVICES("AI Services"),
        REPORTING("Reporting"),
        AUTHENTICATION("Authentication"),
        FILE_UPLOAD("File Upload"),
        EMERGENCY("Emergency");
        
        private final String displayName;
        
        ConfigCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Check if this configuration matches the given criteria
     */
    public boolean matches(String service, String endpoint, String role, Long clinic) {
        if (!active) return false;
        if (!serviceName.equals(service)) return false;
        if (!endpointPattern.equals("/**") && !endpoint.matches(endpointPattern.replace("**", ".*"))) return false;
        if (userRole != null && !userRole.equals(role)) return false;
        if (clinicId != null && !clinicId.equals(clinic)) return false;
        return true;
    }
    
    /**
     * Get configuration summary for display
     */
    public String getSummary() {
        return String.format("%s: %d %s per %d seconds for %s%s", 
            configName, 
            maxRequests, 
            limitType.getDisplayName().toLowerCase(),
            timeWindowSeconds,
            serviceName,
            userRole != null ? " (" + userRole + ")" : "");
    }
}
