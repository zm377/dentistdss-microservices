package com.dentistdss.reporting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.dentistdss.reporting.dto.ReportRequest;
import com.dentistdss.reporting.dto.ReportResult;
import com.dentistdss.reporting.model.ReportExecution;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Email Delivery Service
 * 
 * Handles automated email delivery of generated reports with attachments.
 * Supports multiple recipients and customizable email templates.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${reporting.email.from}")
    private String fromEmail;

    @Value("${reporting.email.max-recipients:50}")
    private int maxRecipients;

    @Value("${reporting.email.attachment-size-limit:25MB}")
    private String attachmentSizeLimit;

    /**
     * Send report email asynchronously
     */
    @Async("emailDeliveryExecutor")
    public void sendReportEmail(ReportRequest request, ReportResult result) {
        log.info("Sending report email for execution: {}", result.getExecutionId());
        
        try {
            // Validate recipients
            if (request.getEmailRecipients() == null || request.getEmailRecipients().isEmpty()) {
                log.warn("No email recipients specified for execution: {}", result.getExecutionId());
                return;
            }
            
            if (request.getEmailRecipients().size() > maxRecipients) {
                log.warn("Too many recipients ({}) for execution: {}, limit is {}", 
                        request.getEmailRecipients().size(), result.getExecutionId(), maxRecipients);
                return;
            }
            
            // Create email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set basic email properties
            helper.setFrom(fromEmail);
            helper.setTo(request.getEmailRecipients().toArray(new String[0]));
            helper.setSubject(generateEmailSubject(request, result));
            helper.setText(generateEmailBody(request, result), true);
            
            // Attach generated files
            attachReportFiles(helper, result);
            
            // Send email
            mailSender.send(message);
            
            log.info("Successfully sent report email for execution: {}", result.getExecutionId());
            
        } catch (Exception e) {
            log.error("Failed to send report email for execution: {}", result.getExecutionId(), e);
            // In a production system, you might want to retry or store failed deliveries
        }
    }

    /**
     * Generate email subject
     */
    private String generateEmailSubject(ReportRequest request, ReportResult result) {
        if (request.getEmailSubject() != null && !request.getEmailSubject().trim().isEmpty()) {
            return request.getEmailSubject();
        }
        
        String templateName = request.getTemplate() != null ? 
                request.getTemplate().getName() : request.getTemplateCode();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        
        return String.format("Report: %s - %s", templateName, timestamp);
    }

    /**
     * Generate email body
     */
    private String generateEmailBody(ReportRequest request, ReportResult result) {
        StringBuilder body = new StringBuilder();
        
        body.append("<html><body>");
        body.append("<h2>Report Generated Successfully</h2>");
        
        // Report details
        body.append("<h3>Report Details</h3>");
        body.append("<table border='1' cellpadding='5' cellspacing='0'>");
        
        if (request.getTemplate() != null) {
            body.append("<tr><td><strong>Report Name:</strong></td><td>")
                .append(request.getTemplate().getName()).append("</td></tr>");
        }
        
        body.append("<tr><td><strong>Template Code:</strong></td><td>")
            .append(request.getTemplateCode()).append("</td></tr>");
        
        body.append("<tr><td><strong>Generated On:</strong></td><td>")
            .append(result.getGeneratedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")))
            .append("</td></tr>");
        
        if (result.getAnalyticsResult() != null) {
            body.append("<tr><td><strong>Total Records:</strong></td><td>")
                .append(result.getAnalyticsResult().getRecordCount()).append("</td></tr>");
            
            body.append("<tr><td><strong>Execution Time:</strong></td><td>")
                .append(result.getAnalyticsResult().getExecutionTimeMs()).append(" ms</td></tr>");
        }
        
        body.append("</table>");
        
        // Parameters
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            body.append("<h3>Report Parameters</h3>");
            body.append("<table border='1' cellpadding='5' cellspacing='0'>");
            
            for (var entry : request.getParameters().entrySet()) {
                body.append("<tr><td><strong>").append(entry.getKey())
                    .append(":</strong></td><td>").append(entry.getValue()).append("</td></tr>");
            }
            
            body.append("</table>");
        }
        
        // Generated files
        if (result.getGeneratedFiles() != null && !result.getGeneratedFiles().isEmpty()) {
            body.append("<h3>Generated Files</h3>");
            body.append("<p>The following files have been generated and are attached to this email:</p>");
            body.append("<ul>");
            
            for (ReportExecution.GeneratedFile file : result.getGeneratedFiles()) {
                body.append("<li>").append(file.getFileName())
                    .append(" (").append(file.getFormat()).append(", ")
                    .append(formatFileSize(file.getFileSize())).append(")</li>");
            }
            
            body.append("</ul>");
        }
        
        // Footer
        body.append("<hr>");
        body.append("<p><small>This report was generated by the DentistDSS Reporting Service. ");
        body.append("If you have any questions, please contact your system administrator.</small></p>");
        
        body.append("</body></html>");
        
        return body.toString();
    }

    /**
     * Attach report files to email
     */
    private void attachReportFiles(MimeMessageHelper helper, ReportResult result) throws Exception {
        if (result.getGeneratedFiles() == null || result.getGeneratedFiles().isEmpty()) {
            return;
        }
        
        long maxSizeBytes = parseFileSize(attachmentSizeLimit);
        long totalSize = 0;
        
        for (ReportExecution.GeneratedFile file : result.getGeneratedFiles()) {
            File attachmentFile = new File(file.getFilePath());
            
            if (!attachmentFile.exists()) {
                log.warn("Generated file not found: {}", file.getFilePath());
                continue;
            }
            
            if (totalSize + file.getFileSize() > maxSizeBytes) {
                log.warn("Attachment size limit exceeded, skipping file: {}", file.getFileName());
                continue;
            }
            
            FileSystemResource fileResource = new FileSystemResource(attachmentFile);
            helper.addAttachment(file.getFileName(), fileResource);
            totalSize += file.getFileSize();
            
            log.debug("Attached file: {} ({})", file.getFileName(), formatFileSize(file.getFileSize()));
        }
    }

    /**
     * Format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Parse file size string to bytes
     */
    private long parseFileSize(String sizeStr) {
        if (sizeStr == null || sizeStr.trim().isEmpty()) {
            return 25 * 1024 * 1024; // Default 25MB
        }
        
        sizeStr = sizeStr.trim().toUpperCase();
        
        if (sizeStr.endsWith("KB")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024;
        } else if (sizeStr.endsWith("MB")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024 * 1024;
        } else if (sizeStr.endsWith("GB")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 2)) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(sizeStr); // Assume bytes
        }
    }
}
