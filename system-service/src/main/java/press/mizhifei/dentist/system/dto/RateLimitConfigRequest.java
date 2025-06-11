package press.mizhifei.dentist.system.dto;

import lombok.*;
import press.mizhifei.dentist.system.model.RateLimitConfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for Rate Limit Configuration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitConfigRequest {
    
    @NotBlank(message = "Configuration name is required")
    private String configName;
    
    @NotBlank(message = "Service name is required")
    private String serviceName;
    
    @NotBlank(message = "Endpoint pattern is required")
    private String endpointPattern;
    
    private String userRole;
    
    private Long clinicId;
    
    @NotNull(message = "Max requests is required")
    @Positive(message = "Max requests must be positive")
    private Long maxRequests;
    
    @NotNull(message = "Time window is required")
    @Positive(message = "Time window must be positive")
    private Long timeWindowSeconds;
    
    @NotNull(message = "Limit type is required")
    private RateLimitConfig.RateLimitType limitType;
    
    @Builder.Default
    private Integer priority = 0;
    
    @Builder.Default
    private Boolean active = true;
    
    private String description;
}
