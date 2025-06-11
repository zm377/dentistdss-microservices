package com.dentistdss.gateway.dto;

import lombok.*;

/**
 * Rate Limit Configuration DTO for API Gateway
 * Simplified version of the system service configuration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitConfigDto {
    
    private Long id;
    private String configName;
    private String serviceName;
    private String endpointPattern;
    private String userRole;
    private Long clinicId;
    private Long maxRequests;
    private Long timeWindowSeconds;
    private RateLimitType limitType;
    private Integer priority;
    private Boolean active;
    private String description;
    
    /**
     * Rate limit types
     */
    public enum RateLimitType {
        REQUEST_COUNT,
        TOKEN_COUNT,
        BANDWIDTH
    }
}
