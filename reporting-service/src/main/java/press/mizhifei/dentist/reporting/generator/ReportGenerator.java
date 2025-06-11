package press.mizhifei.dentist.reporting.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import press.mizhifei.dentist.reporting.analytics.AnalyticsEngine;
import press.mizhifei.dentist.reporting.dto.AnalyticsResult;
import press.mizhifei.dentist.reporting.dto.ReportRequest;
import press.mizhifei.dentist.reporting.dto.ReportResult;
import press.mizhifei.dentist.reporting.model.ReportExecution;
import press.mizhifei.dentist.reporting.model.ReportTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Report Generator Service
 * 
 * Orchestrates the report generation process including data retrieval,
 * formatting, and file generation in multiple formats.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerator {

    private final AnalyticsEngine analyticsEngine;
    private final PdfReportGenerator pdfGenerator;
    private final ExcelReportGenerator excelGenerator;
    private final CsvReportGenerator csvGenerator;

    @Value("${reporting.export.temp-directory:${java.io.tmpdir}/reporting}")
    private String tempDirectory;

    @Value("${reporting.export.max-file-size:50MB}")
    private String maxFileSize;

    /**
     * Generate report asynchronously
     */
    @Async("reportGenerationExecutor")
    public CompletableFuture<ReportResult> generateReportAsync(ReportRequest request) {
        log.info("Starting async report generation for template: {}", request.getTemplateCode());
        
        try {
            ReportResult result = generateReport(request);
            log.info("Completed async report generation for template: {}", request.getTemplateCode());
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Error in async report generation for template: {}", request.getTemplateCode(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate report synchronously
     */
    public ReportResult generateReport(ReportRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Generating report for template: {}", request.getTemplateCode());
            
            // Step 1: Execute analytics query
            AnalyticsResult analyticsResult = executeAnalyticsQuery(request);
            
            // Step 2: Generate files in requested formats
            List<ReportExecution.GeneratedFile> generatedFiles = generateFiles(request, analyticsResult);
            
            // Step 3: Create execution metrics
            ReportExecution.ExecutionMetrics metrics = createExecutionMetrics(
                startTime, analyticsResult, generatedFiles);
            
            return ReportResult.builder()
                    .templateCode(request.getTemplateCode())
                    .executionId(request.getExecutionId())
                    .status(ReportExecution.ExecutionStatus.COMPLETED)
                    .generatedFiles(generatedFiles)
                    .metrics(metrics)
                    .analyticsResult(analyticsResult)
                    .generatedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating report for template: {}", request.getTemplateCode(), e);
            
            return ReportResult.builder()
                    .templateCode(request.getTemplateCode())
                    .executionId(request.getExecutionId())
                    .status(ReportExecution.ExecutionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Execute analytics query based on template and parameters
     */
    private AnalyticsResult executeAnalyticsQuery(ReportRequest request) {
        // Build analytics query from template and parameters
        String sql = buildSqlFromTemplate(request.getTemplate(), request.getParameters());
        
        press.mizhifei.dentist.reporting.dto.AnalyticsQuery query = 
            press.mizhifei.dentist.reporting.dto.AnalyticsQuery.builder()
                .queryId(request.getTemplateCode() + "_" + System.currentTimeMillis())
                .sql(sql)
                .parameters(extractParameterValues(request.getParameters()))
                .readOnly(true)
                .build();
        
        return analyticsEngine.executeQuery(query);
    }

    /**
     * Generate files in all requested formats
     */
    private List<ReportExecution.GeneratedFile> generateFiles(
            ReportRequest request, AnalyticsResult analyticsResult) {
        
        List<ReportExecution.GeneratedFile> generatedFiles = new ArrayList<>();
        
        for (ReportTemplate.ReportFormat format : request.getRequestedFormats()) {
            try {
                ReportExecution.GeneratedFile file = generateFileForFormat(
                    request, analyticsResult, format);
                generatedFiles.add(file);
                
            } catch (Exception e) {
                log.error("Error generating {} file for template: {}", 
                    format, request.getTemplateCode(), e);
                // Continue with other formats even if one fails
            }
        }
        
        return generatedFiles;
    }

    /**
     * Generate file for specific format
     */
    private ReportExecution.GeneratedFile generateFileForFormat(
            ReportRequest request, AnalyticsResult analyticsResult, ReportTemplate.ReportFormat format) {
        
        long startTime = System.currentTimeMillis();
        
        switch (format) {
            case PDF:
                return pdfGenerator.generatePdf(request, analyticsResult);
            case EXCEL:
                return excelGenerator.generateExcel(request, analyticsResult);
            case CSV:
                return csvGenerator.generateCsv(request, analyticsResult);
            default:
                throw new UnsupportedOperationException("Unsupported format: " + format);
        }
    }

    /**
     * Build SQL query from template and parameters
     */
    private String buildSqlFromTemplate(ReportTemplate template, Map<String, Object> parameters) {
        String sql = template.getQueryTemplate();
        
        // Simple parameter substitution (in production, use a proper template engine)
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            sql = sql.replace(placeholder, value);
        }
        
        return sql;
    }

    /**
     * Extract parameter values for JDBC query
     */
    private List<Object> extractParameterValues(Map<String, Object> parameters) {
        // Extract values in the order they appear in the SQL
        // This is a simplified implementation
        return new ArrayList<>(parameters.values());
    }

    /**
     * Create execution metrics
     */
    private ReportExecution.ExecutionMetrics createExecutionMetrics(
            long startTime, AnalyticsResult analyticsResult, List<ReportExecution.GeneratedFile> files) {
        
        long totalTime = System.currentTimeMillis() - startTime;
        long totalFileSize = files.stream().mapToLong(ReportExecution.GeneratedFile::getFileSize).sum();
        
        return ReportExecution.ExecutionMetrics.builder()
                .queryExecutionTimeMs(analyticsResult.getExecutionTimeMs())
                .totalExecutionTimeMs(totalTime)
                .recordCount(analyticsResult.getRecordCount())
                .memoryUsedBytes(totalFileSize)
                .executorThread(Thread.currentThread().getName())
                .build();
    }

    /**
     * Ensure temp directory exists
     */
    private void ensureTempDirectoryExists() {
        File dir = new File(tempDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create temp directory: " + tempDirectory);
            }
        }
    }
}
