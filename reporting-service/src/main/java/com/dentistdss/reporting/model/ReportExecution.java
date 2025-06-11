package com.dentistdss.reporting.model;

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
 * Report Execution Document
 * 
 * Tracks report generation requests, execution status, and results.
 * Provides audit trail and enables monitoring of report performance.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "report_executions")
public class ReportExecution {

    @Id
    private String id;

    /**
     * Reference to the report template
     */
    @Indexed
    private String templateId;

    /**
     * Template code for quick reference
     */
    @Indexed
    private String templateCode;

    /**
     * User who requested the report
     */
    @Indexed
    private Long requestedBy;

    /**
     * Clinic context for the report
     */
    @Indexed
    private Long clinicId;

    /**
     * Report execution status
     */
    @Indexed
    private ExecutionStatus status;

    /**
     * Report parameters used for execution
     */
    private Map<String, Object> parameters;

    /**
     * Requested output formats
     */
    private List<ReportTemplate.ReportFormat> requestedFormats;

    /**
     * Generated file information
     */
    private List<GeneratedFile> generatedFiles;

    /**
     * Execution metrics
     */
    private ExecutionMetrics metrics;

    /**
     * Error information if execution failed
     */
    private ErrorInfo errorInfo;

    /**
     * Email delivery information
     */
    private EmailDelivery emailDelivery;

    /**
     * Whether this is a scheduled execution
     */
    @Builder.Default
    private Boolean scheduled = false;

    /**
     * Schedule ID if this is a scheduled report
     */
    private String scheduleId;

    /**
     * Request timestamp
     */
    @Indexed
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    /**
     * Execution start timestamp
     */
    private LocalDateTime startedAt;

    /**
     * Execution completion timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Execution status enumeration
     */
    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Generated file information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedFile {
        private ReportTemplate.ReportFormat format;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String contentType;
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;
    }

    /**
     * Execution performance metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionMetrics {
        private Long queryExecutionTimeMs;
        private Long dataProcessingTimeMs;
        private Long fileGenerationTimeMs;
        private Long totalExecutionTimeMs;
        private Integer recordCount;
        private Long memoryUsedBytes;
        private String executorThread;
    }

    /**
     * Error information for failed executions
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private String errorCode;
        private String errorMessage;
        private String stackTrace;
        private LocalDateTime errorTimestamp;
        private String errorCategory; // QUERY_ERROR, GENERATION_ERROR, DELIVERY_ERROR
    }

    /**
     * Email delivery tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailDelivery {
        private List<String> recipients;
        private String subject;
        private Boolean delivered;
        private LocalDateTime sentAt;
        private String deliveryStatus;
        private String errorMessage;
        private List<String> attachmentFileNames;
    }
}
