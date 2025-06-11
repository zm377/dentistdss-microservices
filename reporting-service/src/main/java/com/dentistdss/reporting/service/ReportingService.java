package com.dentistdss.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.reporting.dto.ReportRequest;
import com.dentistdss.reporting.dto.ReportResult;
import com.dentistdss.reporting.generator.ReportGenerator;
import com.dentistdss.reporting.model.ReportExecution;
import com.dentistdss.reporting.model.ReportTemplate;
import com.dentistdss.reporting.repository.ReportExecutionRepository;
import com.dentistdss.reporting.repository.ReportTemplateRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main Reporting Service
 * 
 * Orchestrates report generation, execution tracking, and result management.
 * Provides both synchronous and asynchronous report generation capabilities.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final ReportGenerator reportGenerator;
    private final ReportTemplateRepository templateRepository;
    private final ReportExecutionRepository executionRepository;
    private final EmailDeliveryService emailDeliveryService;
    private final SecurityService securityService;

    /**
     * Generate report asynchronously
     */
    public CompletableFuture<ReportResult> generateReportAsync(ReportRequest request) {
        log.info("Starting async report generation for template: {}", request.getTemplateCode());
        
        // Validate request and permissions
        validateReportRequest(request);
        
        // Create execution record
        ReportExecution execution = createExecutionRecord(request);
        
        // Generate execution ID if not provided
        if (request.getExecutionId() == null) {
            request.setExecutionId(execution.getId());
        }
        
        // Load template
        ReportTemplate template = getReportTemplate(request.getTemplateCode());
        request.setTemplate(template);
        
        // Start async generation
        return reportGenerator.generateReportAsync(request)
                .thenApply(result -> {
                    // Update execution record
                    updateExecutionRecord(execution.getId(), result);
                    
                    // Send email if requested
                    if (request.getEmailDelivery() && !request.getEmailRecipients().isEmpty()) {
                        emailDeliveryService.sendReportEmail(request, result);
                    }
                    
                    return result;
                })
                .exceptionally(throwable -> {
                    log.error("Error in async report generation: {}", throwable.getMessage(), throwable);
                    
                    // Update execution record with error
                    ReportResult errorResult = ReportResult.builder()
                            .templateCode(request.getTemplateCode())
                            .executionId(request.getExecutionId())
                            .status(ReportExecution.ExecutionStatus.FAILED)
                            .errorMessage(throwable.getMessage())
                            .generatedAt(LocalDateTime.now())
                            .build();
                    
                    updateExecutionRecord(execution.getId(), errorResult);
                    return errorResult;
                });
    }

    /**
     * Generate report synchronously
     */
    public ReportResult generateReport(ReportRequest request) {
        log.info("Starting sync report generation for template: {}", request.getTemplateCode());
        
        // Validate request and permissions
        validateReportRequest(request);
        
        // Create execution record
        ReportExecution execution = createExecutionRecord(request);
        
        // Generate execution ID if not provided
        if (request.getExecutionId() == null) {
            request.setExecutionId(execution.getId());
        }
        
        // Load template
        ReportTemplate template = getReportTemplate(request.getTemplateCode());
        request.setTemplate(template);
        
        try {
            // Generate report
            ReportResult result = reportGenerator.generateReport(request);
            
            // Update execution record
            updateExecutionRecord(execution.getId(), result);
            
            // Send email if requested
            if (request.getEmailDelivery() && !request.getEmailRecipients().isEmpty()) {
                emailDeliveryService.sendReportEmail(request, result);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in sync report generation: {}", e.getMessage(), e);
            
            // Update execution record with error
            ReportResult errorResult = ReportResult.builder()
                    .templateCode(request.getTemplateCode())
                    .executionId(request.getExecutionId())
                    .status(ReportExecution.ExecutionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            updateExecutionRecord(execution.getId(), errorResult);
            throw new ReportGenerationException("Failed to generate report", e);
        }
    }

    /**
     * Get report execution status
     */
    public Optional<ReportExecution> getExecutionStatus(String executionId) {
        return executionRepository.findById(executionId);
    }

    /**
     * Get report executions for a user
     */
    public List<ReportExecution> getUserReportExecutions(Long userId, int page, int size) {
        return executionRepository.findByRequestedByOrderByRequestedAtDesc(userId, 
                org.springframework.data.domain.PageRequest.of(page, size));
    }

    /**
     * Get report executions for a clinic
     */
    public List<ReportExecution> getClinicReportExecutions(Long clinicId, int page, int size) {
        return executionRepository.findByClinicIdOrderByRequestedAtDesc(clinicId,
                org.springframework.data.domain.PageRequest.of(page, size));
    }

    /**
     * Get available report templates
     */
    @Cacheable(value = "reportTemplates", cacheManager = "caffeineCacheManager")
    public List<ReportTemplate> getAvailableTemplates(List<String> userRoles) {
        return templateRepository.findByActiveAndAllowedRolesIn(true, userRoles);
    }

    /**
     * Get report template by code
     */
    @Cacheable(value = "reportTemplates", key = "#templateCode", cacheManager = "caffeineCacheManager")
    public ReportTemplate getReportTemplate(String templateCode) {
        return templateRepository.findByTemplateCodeAndActive(templateCode, true)
                .orElseThrow(() -> new ReportTemplateNotFoundException("Template not found: " + templateCode));
    }

    /**
     * Validate report request
     */
    private void validateReportRequest(ReportRequest request) {
        if (request.getTemplateCode() == null || request.getTemplateCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Template code is required");
        }
        
        if (request.getRequestedFormats() == null || request.getRequestedFormats().isEmpty()) {
            throw new IllegalArgumentException("At least one output format must be specified");
        }
        
        if (request.getParameters() == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        // Check user permissions
        if (!securityService.canAccessTemplate(request.getTemplateCode(), request.getRequestedBy())) {
            throw new SecurityException("User does not have permission to access this template");
        }
    }

    /**
     * Create execution record
     */
    @Transactional
    private ReportExecution createExecutionRecord(ReportRequest request) {
        ReportExecution execution = ReportExecution.builder()
                .templateCode(request.getTemplateCode())
                .requestedBy(request.getRequestedBy())
                .clinicId(request.getClinicId())
                .status(ReportExecution.ExecutionStatus.PENDING)
                .parameters(request.getParameters())
                .requestedFormats(request.getRequestedFormats())
                .scheduled(false)
                .requestedAt(LocalDateTime.now())
                .build();
        
        return executionRepository.save(execution);
    }

    /**
     * Update execution record with results
     */
    @Transactional
    private void updateExecutionRecord(String executionId, ReportResult result) {
        Optional<ReportExecution> executionOpt = executionRepository.findById(executionId);
        if (executionOpt.isPresent()) {
            ReportExecution execution = executionOpt.get();
            execution.setStatus(result.getStatus());
            execution.setGeneratedFiles(result.getGeneratedFiles());
            execution.setMetrics(result.getMetrics());
            execution.setCompletedAt(LocalDateTime.now());
            
            if (result.getErrorMessage() != null) {
                execution.setErrorInfo(ReportExecution.ErrorInfo.builder()
                        .errorMessage(result.getErrorMessage())
                        .errorTimestamp(LocalDateTime.now())
                        .errorCategory("GENERATION_ERROR")
                        .build());
            }
            
            executionRepository.save(execution);
        }
    }

    /**
     * Custom exceptions
     */
    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ReportTemplateNotFoundException extends RuntimeException {
        public ReportTemplateNotFoundException(String message) {
            super(message);
        }
    }
}
