package press.mizhifei.dentist.systemadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.systemadmin.model.SystemParameter;

import java.time.LocalDateTime;

/**
 * Response DTO for System Parameter operations
 * 
 * Comprehensive data transfer for system parameter administration responses
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameterResponse {
    
    private Long id;
    private String parameterKey;
    private String parameterName;
    private String parameterValue;
    private String displayValue;
    private String defaultValue;
    private SystemParameter.ParameterDataType dataType;
    private String dataTypeDisplayName;
    private SystemParameter.ParameterCategory category;
    private String categoryDisplayName;
    private String serviceName;
    private String environment;
    private String description;
    private Boolean sensitive;
    private Boolean required;
    private Boolean active;
    private Boolean requiresApproval;
    private String validationPattern;
    private Double minValue;
    private Double maxValue;
    private String allowedValues;
    private Integer version;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Object typedValue;
    private String scope;
    
    /**
     * Create from entity
     */
    public static SystemParameterResponse fromEntity(SystemParameter entity) {
        return SystemParameterResponse.builder()
                .id(entity.getId())
                .parameterKey(entity.getParameterKey())
                .parameterName(entity.getParameterName())
                .parameterValue(entity.getParameterValue())
                .displayValue(entity.getDisplayValue())
                .defaultValue(entity.getDefaultValue())
                .dataType(entity.getDataType())
                .dataTypeDisplayName(entity.getDataType().getDisplayName())
                .category(entity.getCategory())
                .categoryDisplayName(entity.getCategory().getDisplayName())
                .serviceName(entity.getServiceName())
                .environment(entity.getEnvironment())
                .description(entity.getDescription())
                .sensitive(entity.getSensitive())
                .required(entity.getRequired())
                .active(entity.getActive())
                .requiresApproval(entity.getRequiresApproval())
                .validationPattern(entity.getValidationPattern())
                .minValue(entity.getMinValue())
                .maxValue(entity.getMaxValue())
                .allowedValues(entity.getAllowedValues())
                .version(entity.getVersion())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .typedValue(entity.getTypedValue())
                .scope(getParameterScope(entity))
                .build();
    }
    
    /**
     * Get parameter scope description
     */
    private static String getParameterScope(SystemParameter entity) {
        if (entity.getServiceName() == null && entity.getEnvironment() == null) {
            return "Global";
        }
        
        StringBuilder scope = new StringBuilder();
        if (entity.getServiceName() != null) {
            scope.append("Service: ").append(entity.getServiceName());
        }
        if (entity.getEnvironment() != null) {
            if (scope.length() > 0) scope.append(", ");
            scope.append("Environment: ").append(entity.getEnvironment());
        }
        
        return scope.toString();
    }
    
    /**
     * Check if this is a global parameter
     */
    public boolean isGlobal() {
        return serviceName == null && environment == null;
    }
    
    /**
     * Check if this is a service-specific parameter
     */
    public boolean isServiceSpecific() {
        return serviceName != null;
    }
    
    /**
     * Check if this is an environment-specific parameter
     */
    public boolean isEnvironmentSpecific() {
        return environment != null;
    }
    
    /**
     * Check if parameter is using default value
     */
    public boolean isUsingDefault() {
        if (parameterValue == null && defaultValue == null) return true;
        if (parameterValue == null) return false;
        return parameterValue.equals(defaultValue);
    }
    
    /**
     * Check if parameter has constraints
     */
    public boolean hasConstraints() {
        return validationPattern != null || 
               minValue != null || 
               maxValue != null || 
               allowedValues != null;
    }
    
    /**
     * Get constraint description
     */
    public String getConstraintDescription() {
        if (!hasConstraints()) return "No constraints";
        
        StringBuilder constraints = new StringBuilder();
        
        if (minValue != null || maxValue != null) {
            constraints.append("Range: ");
            if (minValue != null) {
                constraints.append("min=").append(minValue);
            }
            if (maxValue != null) {
                if (minValue != null) constraints.append(", ");
                constraints.append("max=").append(maxValue);
            }
        }
        
        if (allowedValues != null) {
            if (constraints.length() > 0) constraints.append("; ");
            constraints.append("Allowed: ").append(allowedValues);
        }
        
        if (validationPattern != null) {
            if (constraints.length() > 0) constraints.append("; ");
            constraints.append("Pattern: ").append(validationPattern);
        }
        
        return constraints.toString();
    }
    
    /**
     * Get parameter status description
     */
    public String getStatusDescription() {
        StringBuilder status = new StringBuilder();
        
        if (!active) {
            status.append("Inactive");
        } else {
            status.append("Active");
        }
        
        if (required) {
            status.append(", Required");
        }
        
        if (sensitive) {
            status.append(", Sensitive");
        }
        
        if (requiresApproval) {
            status.append(", Requires Approval");
        }
        
        return status.toString();
    }
    
    /**
     * Get formatted value with type information
     */
    public String getFormattedValue() {
        if (sensitive && parameterValue != null) {
            return "***MASKED***";
        }
        
        if (parameterValue == null) {
            return defaultValue != null ? defaultValue + " (default)" : "Not set";
        }
        
        return parameterValue;
    }
}
