package com.dentistdss.reporting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.reporting.dto.ApiResponse;
import com.dentistdss.reporting.dto.ReportRequest;
import com.dentistdss.reporting.dto.ReportResult;
import com.dentistdss.reporting.model.ReportExecution;
import com.dentistdss.reporting.model.ReportTemplate;
import com.dentistdss.reporting.service.ReportingService;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Reporting Controller
 * 
 * REST API for report generation, template management, and execution tracking.
 * Provides both synchronous and asynchronous report generation capabilities.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Advanced analytics and report generation operations")
public class ReportingController {

    private final ReportingService reportingService;

    /**
     * Generate report asynchronously
     */
    @PostMapping("/generate/async")
    @Operation(summary = "Generate report asynchronously", 
               description = "Starts async report generation and returns execution ID for tracking")
    public ResponseEntity<ApiResponse<String>> generateReportAsync(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication) {
        
        log.info("Async report generation requested for template: {}", request.getTemplateCode());
        
        try {
            // Set user context
            Long userId = extractUserId(authentication);
            request.setRequestedBy(userId);
            
            // Start async generation
            CompletableFuture<ReportResult> future = reportingService.generateReportAsync(request);
            
            // Return execution ID immediately
            return ResponseEntity.accepted()
                    .body(ApiResponse.success(request.getExecutionId(), 
                            "Report generation started. Use execution ID to track progress."));
                            
        } catch (Exception e) {
            log.error("Error starting async report generation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to start report generation: " + e.getMessage()));
        }
    }

    /**
     * Generate report synchronously
     */
    @PostMapping("/generate/sync")
    @Operation(summary = "Generate report synchronously", 
               description = "Generates report and returns result immediately (may take time)")
    public ResponseEntity<ApiResponse<ReportResult>> generateReportSync(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication) {
        
        log.info("Sync report generation requested for template: {}", request.getTemplateCode());
        
        try {
            // Set user context
            Long userId = extractUserId(authentication);
            request.setRequestedBy(userId);
            request.setAsync(false);
            
            // Generate report
            ReportResult result = reportingService.generateReport(request);
            
            return ResponseEntity.ok(ApiResponse.success(result, "Report generated successfully"));
            
        } catch (Exception e) {
            log.error("Error in sync report generation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    /**
     * Get report execution status
     */
    @GetMapping("/executions/{executionId}")
    @Operation(summary = "Get report execution status", 
               description = "Retrieves the current status and details of a report execution")
    public ResponseEntity<ApiResponse<ReportExecution>> getExecutionStatus(
            @PathVariable String executionId) {
        
        log.debug("Getting execution status for: {}", executionId);
        
        Optional<ReportExecution> execution = reportingService.getExecutionStatus(executionId);
        
        if (execution.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(execution.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get user's report executions
     */
    @GetMapping("/executions")
    @Operation(summary = "Get user's report executions", 
               description = "Retrieves report execution history for the current user")
    public ResponseEntity<ApiResponse<List<ReportExecution>>> getUserExecutions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.debug("Getting executions for user: {}", userId);
        
        List<ReportExecution> executions = reportingService.getUserReportExecutions(userId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    /**
     * Get clinic's report executions
     */
    @GetMapping("/executions/clinic/{clinicId}")
    @Operation(summary = "Get clinic's report executions", 
               description = "Retrieves report execution history for a specific clinic")
    public ResponseEntity<ApiResponse<List<ReportExecution>>> getClinicExecutions(
            @PathVariable Long clinicId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.debug("Getting executions for clinic: {} by user: {}", clinicId, userId);
        
        // TODO: Add authorization check for clinic access
        
        List<ReportExecution> executions = reportingService.getClinicReportExecutions(clinicId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    /**
     * Get available report templates
     */
    @GetMapping("/templates")
    @Operation(summary = "Get available report templates", 
               description = "Retrieves report templates accessible to the current user")
    public ResponseEntity<ApiResponse<List<ReportTemplate>>> getAvailableTemplates(
            Authentication authentication) {
        
        // TODO: Extract user roles from authentication
        List<String> userRoles = List.of("DENTIST", "CLINIC_ADMIN"); // Placeholder
        
        List<ReportTemplate> templates = reportingService.getAvailableTemplates(userRoles);
        
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    /**
     * Get specific report template
     */
    @GetMapping("/templates/{templateCode}")
    @Operation(summary = "Get report template", 
               description = "Retrieves details of a specific report template")
    public ResponseEntity<ApiResponse<ReportTemplate>> getReportTemplate(
            @PathVariable String templateCode) {
        
        try {
            ReportTemplate template = reportingService.getReportTemplate(templateCode);
            return ResponseEntity.ok(ApiResponse.success(template));
        } catch (ReportingService.ReportTemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download generated report file
     */
    @GetMapping("/files/{executionId}/{fileName}")
    @Operation(summary = "Download report file", 
               description = "Downloads a generated report file")
    public ResponseEntity<Resource> downloadReportFile(
            @PathVariable String executionId,
            @PathVariable String fileName,
            Authentication authentication) {
        
        log.debug("Download requested for execution: {}, file: {}", executionId, fileName);
        
        try {
            // Get execution details
            Optional<ReportExecution> executionOpt = reportingService.getExecutionStatus(executionId);
            if (executionOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ReportExecution execution = executionOpt.get();
            
            // TODO: Add authorization check
            
            // Find the requested file
            Optional<ReportExecution.GeneratedFile> fileOpt = execution.getGeneratedFiles().stream()
                    .filter(f -> f.getFileName().equals(fileName))
                    .findFirst();
            
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ReportExecution.GeneratedFile generatedFile = fileOpt.get();
            File file = new File(generatedFile.getFilePath());
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(generatedFile.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + generatedFile.getFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service health check endpoint")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Reporting Service is healthy"));
    }

    /**
     * Extract user ID from authentication
     */
    private Long extractUserId(Authentication authentication) {
        // TODO: Implement proper user ID extraction from JWT token
        // This is a placeholder implementation
        if (authentication != null && authentication.getName() != null) {
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException e) {
                log.warn("Could not parse user ID from authentication: {}", authentication.getName());
            }
        }
        return 1L; // Placeholder
    }
}
