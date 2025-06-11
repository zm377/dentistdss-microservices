package com.dentistdss.systemadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Enhanced System Parameter Configuration Entity
 * 
 * Comprehensive system parameter management for all microservices
 * with support for different data types and validation
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
@Table(name = "system_parameters", indexes = {
    @Index(name = "idx_category_key", columnList = "category, parameterKey"),
    @Index(name = "idx_service_name", columnList = "serviceName"),
    @Index(name = "idx_environment", columnList = "environment"),
    @Index(name = "idx_active", columnList = "active")
})
public class SystemParameter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique parameter key
     */
    @NotBlank(message = "Parameter key is required")
    @Column(unique = true, nullable = false, length = 100)
    private String parameterKey;
    
    /**
     * Parameter display name
     */
    @NotBlank(message = "Parameter name is required")
    @Column(nullable = false, length = 200)
    private String parameterName;
    
    /**
     * Parameter value (stored as string, converted based on dataType)
     */
    @Column(columnDefinition = "TEXT")
    private String parameterValue;
    
    /**
     * Default value for this parameter
     */
    @Column(columnDefinition = "TEXT")
    private String defaultValue;
    
    /**
     * Data type of the parameter
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Data type is required")
    @Column(nullable = false, length = 20)
    private ParameterDataType dataType;
    
    /**
     * Parameter category for grouping
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category is required")
    @Column(nullable = false, length = 30)
    private ParameterCategory category;
    
    /**
     * Service this parameter applies to (null for global parameters)
     */
    @Column(length = 50)
    private String serviceName;
    
    /**
     * Environment this parameter applies to (null for all environments)
     */
    @Column(length = 20)
    private String environment;
    
    /**
     * Parameter description
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * Whether this parameter is sensitive (passwords, API keys, etc.)
     */
    @Builder.Default
    private Boolean sensitive = false;
    
    /**
     * Whether this parameter is required
     */
    @Builder.Default
    private Boolean required = false;
    
    /**
     * Whether this parameter is active
     */
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Whether this parameter requires approval to change
     */
    @Builder.Default
    private Boolean requiresApproval = false;
    
    /**
     * Validation pattern (regex) for the parameter value
     */
    @Column(length = 500)
    private String validationPattern;
    
    /**
     * Minimum value (for numeric types)
     */
    private Double minValue;
    
    /**
     * Maximum value (for numeric types)
     */
    private Double maxValue;
    
    /**
     * Allowed values (comma-separated for enum-like parameters)
     */
    @Column(length = 1000)
    private String allowedValues;
    
    /**
     * Version for configuration tracking
     */
    @Builder.Default
    private Integer version = 1;
    
    /**
     * User who created this parameter
     */
    @Column(length = 100)
    private String createdBy;
    
    /**
     * User who last updated this parameter
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
     * Parameter data types
     */
    public enum ParameterDataType {
        STRING("String"),
        INTEGER("Integer"),
        LONG("Long"),
        DOUBLE("Double"),
        BOOLEAN("Boolean"),
        JSON("JSON"),
        ENCRYPTED("Encrypted");
        
        private final String displayName;
        
        ParameterDataType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Parameter categories
     */
    public enum ParameterCategory {
        AI_CONFIGURATION("AI Configuration"),
        EMAIL_SETTINGS("Email Settings"),
        FILE_UPLOAD("File Upload"),
        SECURITY("Security"),
        AUDIT_LOGGING("Audit Logging"),
        PERFORMANCE("Performance"),
        INTEGRATION("Integration"),
        UI_SETTINGS("UI Settings"),
        BUSINESS_RULES("Business Rules"),
        SYSTEM_LIMITS("System Limits");
        
        private final String displayName;
        
        ParameterCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Get typed value based on data type
     */
    public Object getTypedValue() {
        if (parameterValue == null) {
            return null;
        }
        
        try {
            switch (dataType) {
                case STRING:
                case JSON:
                case ENCRYPTED:
                    return parameterValue;
                case INTEGER:
                    return Integer.valueOf(parameterValue);
                case LONG:
                    return Long.valueOf(parameterValue);
                case DOUBLE:
                    return Double.valueOf(parameterValue);
                case BOOLEAN:
                    return Boolean.valueOf(parameterValue);
                default:
                    return parameterValue;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Validate parameter value against constraints
     */
    public boolean isValidValue(String value) {
        if (value == null) {
            return !required;
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
        
        // Check numeric ranges
        if (dataType == ParameterDataType.INTEGER || dataType == ParameterDataType.LONG || dataType == ParameterDataType.DOUBLE) {
            try {
                double numValue = Double.parseDouble(value);
                if (minValue != null && numValue < minValue) {
                    return false;
                }
                if (maxValue != null && numValue > maxValue) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get display value (masked for sensitive parameters)
     */
    public String getDisplayValue() {
        if (sensitive && parameterValue != null) {
            return "***MASKED***";
        }
        return parameterValue;
    }
}
