package press.mizhifei.dentist.systemadmin.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.systemadmin.model.SystemParameter;

/**
 * Request DTO for System Parameter operations
 * 
 * Comprehensive validation and data transfer for system parameter administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameterRequest {
    
    @NotBlank(message = "Parameter key is required")
    @Size(max = 100, message = "Parameter key must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Parameter key can only contain letters, numbers, dots, underscores, and hyphens")
    private String parameterKey;
    
    @NotBlank(message = "Parameter name is required")
    @Size(max = 200, message = "Parameter name must not exceed 200 characters")
    private String parameterName;
    
    private String parameterValue;
    
    private String defaultValue;
    
    @NotNull(message = "Data type is required")
    private SystemParameter.ParameterDataType dataType;
    
    @NotNull(message = "Category is required")
    private SystemParameter.ParameterCategory category;
    
    @Size(max = 50, message = "Service name must not exceed 50 characters")
    private String serviceName;
    
    @Size(max = 20, message = "Environment must not exceed 20 characters")
    @Pattern(regexp = "^(dev|docker|prod)$", message = "Environment must be one of: dev, docker, prod")
    private String environment;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Boolean sensitive;
    
    private Boolean required;
    
    private Boolean active;
    
    private Boolean requiresApproval;
    
    @Size(max = 500, message = "Validation pattern must not exceed 500 characters")
    private String validationPattern;
    
    @DecimalMin(value = "0", message = "Minimum value cannot be negative")
    private Double minValue;
    
    @DecimalMin(value = "0", message = "Maximum value cannot be negative")
    private Double maxValue;
    
    @Size(max = 1000, message = "Allowed values must not exceed 1000 characters")
    private String allowedValues;
    
    /**
     * Validate the request data
     */
    public boolean isValid() {
        if (parameterKey == null || parameterKey.trim().isEmpty()) return false;
        if (parameterName == null || parameterName.trim().isEmpty()) return false;
        if (dataType == null) return false;
        if (category == null) return false;
        
        // Validate min/max values
        if (minValue != null && maxValue != null && minValue > maxValue) {
            return false;
        }
        
        // Validate parameter value against constraints
        if (parameterValue != null && !isValidParameterValue(parameterValue)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate parameter value against data type and constraints
     */
    private boolean isValidParameterValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return !Boolean.TRUE.equals(required);
        }
        
        // Check validation pattern
        if (validationPattern != null && !value.matches(validationPattern)) {
            return false;
        }
        
        // Check allowed values
        if (allowedValues != null) {
            String[] allowed = allowedValues.split(",");
            boolean found = false;
            for (String allowedValue : allowed) {
                if (allowedValue.trim().equals(value)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        
        // Check data type specific validation
        try {
            switch (dataType) {
                case INTEGER:
                    int intValue = Integer.parseInt(value);
                    if (minValue != null && intValue < minValue) return false;
                    if (maxValue != null && intValue > maxValue) return false;
                    break;
                case LONG:
                    long longValue = Long.parseLong(value);
                    if (minValue != null && longValue < minValue) return false;
                    if (maxValue != null && longValue > maxValue) return false;
                    break;
                case DOUBLE:
                    double doubleValue = Double.parseDouble(value);
                    if (minValue != null && doubleValue < minValue) return false;
                    if (maxValue != null && doubleValue > maxValue) return false;
                    break;
                case BOOLEAN:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        return false;
                    }
                    break;
                case JSON:
                    // Basic JSON validation - should start with { or [
                    if (!value.trim().startsWith("{") && !value.trim().startsWith("[")) {
                        return false;
                    }
                    break;
                case STRING:
                case ENCRYPTED:
                default:
                    // No additional validation for string types
                    break;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Convert to entity
     */
    public SystemParameter toEntity() {
        return SystemParameter.builder()
                .parameterKey(parameterKey)
                .parameterName(parameterName)
                .parameterValue(parameterValue)
                .defaultValue(defaultValue)
                .dataType(dataType)
                .category(category)
                .serviceName(serviceName)
                .environment(environment)
                .description(description)
                .sensitive(sensitive != null ? sensitive : false)
                .required(required != null ? required : false)
                .active(active != null ? active : true)
                .requiresApproval(requiresApproval != null ? requiresApproval : false)
                .validationPattern(validationPattern)
                .minValue(minValue)
                .maxValue(maxValue)
                .allowedValues(allowedValues)
                .build();
    }
    
    /**
     * Create from entity
     */
    public static SystemParameterRequest fromEntity(SystemParameter entity) {
        return SystemParameterRequest.builder()
                .parameterKey(entity.getParameterKey())
                .parameterName(entity.getParameterName())
                .parameterValue(entity.getParameterValue())
                .defaultValue(entity.getDefaultValue())
                .dataType(entity.getDataType())
                .category(entity.getCategory())
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
                .build();
    }
}
