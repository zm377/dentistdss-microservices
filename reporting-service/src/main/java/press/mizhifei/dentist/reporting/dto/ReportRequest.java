package press.mizhifei.dentist.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.reporting.model.ReportTemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Report Request DTO
 * 
 * Encapsulates all parameters needed to generate a report.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    
    /**
     * Unique execution identifier
     */
    private String executionId;
    
    /**
     * Template code to use for report generation
     */
    @NotBlank(message = "Template code is required")
    private String templateCode;
    
    /**
     * Report template (populated by service)
     */
    private ReportTemplate template;
    
    /**
     * Report parameters
     */
    @NotNull(message = "Parameters cannot be null")
    private Map<String, Object> parameters;
    
    /**
     * Requested output formats
     */
    @NotEmpty(message = "At least one format must be specified")
    private List<ReportTemplate.ReportFormat> requestedFormats;
    
    /**
     * User requesting the report
     */
    private Long requestedBy;
    
    /**
     * Clinic context
     */
    private Long clinicId;
    
    /**
     * Whether to send report via email
     */
    @Builder.Default
    private Boolean emailDelivery = false;
    
    /**
     * Email recipients (if email delivery is enabled)
     */
    private List<String> emailRecipients;
    
    /**
     * Custom email subject
     */
    private String emailSubject;
    
    /**
     * Whether this is an async request
     */
    @Builder.Default
    private Boolean async = true;
    
    /**
     * Priority level for processing
     */
    @Builder.Default
    private ReportPriority priority = ReportPriority.NORMAL;
    
    /**
     * Report priority levels
     */
    public enum ReportPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
