package press.mizhifei.dentist.system.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Rate Limiting Configuration Entity
 * Stores rate limiting rules for different services, user roles, and endpoints
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
@Table(name = "rate_limit_configs")
public class RateLimitConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Configuration name/identifier
     */
    @Column(unique = true, nullable = false)
    private String configName;
    
    /**
     * Service name (e.g., "genai-service", "reporting-service")
     */
    @Column(nullable = false)
    private String serviceName;
    
    /**
     * Endpoint pattern (e.g., "/api/genai/**", "/api/reports/**")
     */
    @Column(nullable = false)
    private String endpointPattern;
    
    /**
     * User role this limit applies to (null for all users)
     */
    private String userRole;
    
    /**
     * Clinic ID this limit applies to (null for all clinics)
     */
    private Long clinicId;
    
    /**
     * Maximum number of requests/tokens allowed
     */
    @Column(nullable = false)
    private Long maxRequests;
    
    /**
     * Time window in seconds
     */
    @Column(nullable = false)
    private Long timeWindowSeconds;
    
    /**
     * Rate limit type: REQUEST_COUNT, TOKEN_COUNT, BANDWIDTH
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
    private String description;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Rate limit types
     */
    public enum RateLimitType {
        /**
         * Limit by number of requests
         */
        REQUEST_COUNT,
        
        /**
         * Limit by token consumption (for AI services)
         */
        TOKEN_COUNT,
        
        /**
         * Limit by bandwidth/data size
         */
        BANDWIDTH
    }
}
