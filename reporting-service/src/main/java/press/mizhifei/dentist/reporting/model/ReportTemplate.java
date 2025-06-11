package press.mizhifei.dentist.reporting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Template Document
 * 
 * Defines reusable report templates with metadata, parameters, and formatting options.
 * Stored in MongoDB for flexible schema and easy template management.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "report_templates")
public class ReportTemplate {

    @Id
    private String id;

    /**
     * Unique template identifier
     */
    @Indexed(unique = true)
    private String templateCode;

    /**
     * Human-readable template name
     */
    private String name;

    /**
     * Template description
     */
    private String description;

    /**
     * Report category (CLINICAL, FINANCIAL, OPERATIONAL, ANALYTICS)
     */
    @Indexed
    private ReportCategory category;

    /**
     * Report type (PATIENT_ANALYTICS, APPOINTMENT_METRICS, REVENUE_REPORT, etc.)
     */
    @Indexed
    private ReportType type;

    /**
     * SQL query template with parameter placeholders
     */
    private String queryTemplate;

    /**
     * Template parameters definition
     */
    private List<TemplateParameter> parameters;

    /**
     * Output format configurations
     */
    private Map<ReportFormat, FormatConfiguration> formatConfigurations;

    /**
     * Chart and visualization configurations
     */
    private List<ChartConfiguration> chartConfigurations;

    /**
     * Access control - roles that can use this template
     */
    private List<String> allowedRoles;

    /**
     * Whether template is active
     */
    @Builder.Default
    private Boolean active = true;

    /**
     * Template version for change tracking
     */
    @Builder.Default
    private Integer version = 1;

    /**
     * Created by user ID
     */
    private Long createdBy;

    /**
     * Last modified by user ID
     */
    private Long modifiedBy;

    /**
     * Creation timestamp
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last modification timestamp
     */
    @Builder.Default
    private LocalDateTime modifiedAt = LocalDateTime.now();

    /**
     * Template parameter definition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateParameter {
        private String name;
        private String label;
        private ParameterType type;
        private Boolean required;
        private Object defaultValue;
        private String description;
        private Map<String, Object> validationRules;
    }

    /**
     * Format-specific configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormatConfiguration {
        private String template;
        private Map<String, Object> options;
        private List<String> includedColumns;
        private Map<String, String> columnMappings;
    }

    /**
     * Chart configuration for visualizations
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartConfiguration {
        private String chartId;
        private ChartType chartType;
        private String title;
        private String xAxisColumn;
        private String yAxisColumn;
        private List<String> seriesColumns;
        private Map<String, Object> chartOptions;
    }

    /**
     * Report categories
     */
    public enum ReportCategory {
        CLINICAL,
        FINANCIAL,
        OPERATIONAL,
        ANALYTICS,
        COMPLIANCE
    }

    /**
     * Specific report types
     */
    public enum ReportType {
        PATIENT_NO_SHOWS,
        APPOINTMENT_UTILIZATION,
        REVENUE_ANALYSIS,
        TREATMENT_COMPLETION,
        AI_USAGE_STATISTICS,
        CLINIC_PERFORMANCE,
        PATIENT_DEMOGRAPHICS,
        BILLING_SUMMARY,
        STAFF_PRODUCTIVITY,
        EQUIPMENT_UTILIZATION
    }

    /**
     * Supported output formats
     */
    public enum ReportFormat {
        PDF,
        EXCEL,
        CSV,
        JSON
    }

    /**
     * Parameter types
     */
    public enum ParameterType {
        STRING,
        INTEGER,
        DECIMAL,
        DATE,
        DATE_RANGE,
        BOOLEAN,
        LIST,
        CLINIC_ID,
        USER_ID
    }

    /**
     * Chart types for visualizations
     */
    public enum ChartType {
        LINE,
        BAR,
        PIE,
        AREA,
        SCATTER,
        HISTOGRAM
    }
}
