package press.mizhifei.dentist.system.dto;

import lombok.*;
import press.mizhifei.dentist.system.model.RateLimitConfig;

import java.time.LocalDateTime;

/**
 * Response DTO for Rate Limit Configuration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Integer priority;
    private Boolean active;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
