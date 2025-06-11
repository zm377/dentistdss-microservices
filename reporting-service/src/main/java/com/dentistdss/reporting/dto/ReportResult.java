package com.dentistdss.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.reporting.model.ReportExecution;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report Result DTO
 * 
 * Contains the results of report generation including files and metrics.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResult {
    
    /**
     * Template code used
     */
    private String templateCode;
    
    /**
     * Execution identifier
     */
    private String executionId;
    
    /**
     * Execution status
     */
    private ReportExecution.ExecutionStatus status;
    
    /**
     * Generated files
     */
    private List<ReportExecution.GeneratedFile> generatedFiles;
    
    /**
     * Execution metrics
     */
    private ReportExecution.ExecutionMetrics metrics;
    
    /**
     * Analytics result data
     */
    private AnalyticsResult analyticsResult;
    
    /**
     * Error message if generation failed
     */
    private String errorMessage;
    
    /**
     * When the report was generated
     */
    private LocalDateTime generatedAt;
    
    /**
     * Email delivery status
     */
    private ReportExecution.EmailDelivery emailDelivery;
}
