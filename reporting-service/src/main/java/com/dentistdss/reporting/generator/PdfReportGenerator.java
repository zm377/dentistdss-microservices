package com.dentistdss.reporting.generator;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.dentistdss.reporting.dto.AnalyticsResult;
import com.dentistdss.reporting.dto.ReportRequest;
import com.dentistdss.reporting.model.ReportExecution;
import com.dentistdss.reporting.model.ReportTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PDF Report Generator
 * 
 * Generates professional PDF reports with tables, charts, and formatting.
 * Uses iText library for advanced PDF generation capabilities.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfReportGenerator {

    @Value("${reporting.export.temp-directory:${java.io.tmpdir}/reporting}")
    private String tempDirectory;

    /**
     * Generate PDF report
     */
    public ReportExecution.GeneratedFile generatePdf(ReportRequest request, AnalyticsResult analyticsResult) {
        log.debug("Generating PDF report for template: {}", request.getTemplateCode());
        
        try {
            String fileName = generateFileName(request, "pdf");
            String filePath = tempDirectory + File.separator + fileName;
            
            // Ensure directory exists
            new File(tempDirectory).mkdirs();
            
            // Create PDF document
            PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add report content
            addReportHeader(document, request);
            addReportMetadata(document, request, analyticsResult);
            addDataTable(document, analyticsResult);
            addReportFooter(document);
            
            document.close();
            
            File file = new File(filePath);
            
            return ReportExecution.GeneratedFile.builder()
                    .format(ReportTemplate.ReportFormat.PDF)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileSize(file.length())
                    .contentType("application/pdf")
                    .generatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Add report header with title and logo
     */
    private void addReportHeader(Document document, ReportRequest request) {
        // Title
        Paragraph title = new Paragraph(request.getTemplate().getName())
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        // Subtitle with generation date
        Paragraph subtitle = new Paragraph("Generated on " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    /**
     * Add report metadata and parameters
     */
    private void addReportMetadata(Document document, ReportRequest request, AnalyticsResult analyticsResult) {
        // Parameters section
        if (!request.getParameters().isEmpty()) {
            document.add(new Paragraph("Report Parameters").setFontSize(14).setBold());
            
            Table paramTable = new Table(2);
            paramTable.setWidth(UnitValue.createPercentValue(100));
            
            // Header
            paramTable.addHeaderCell(new Cell().add(new Paragraph("Parameter").setBold()));
            paramTable.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));
            
            // Parameters
            for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                paramTable.addCell(new Cell().add(new Paragraph(entry.getKey())));
                paramTable.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue()))));
            }
            
            document.add(paramTable);
            document.add(new Paragraph("\n"));
        }
        
        // Summary section
        document.add(new Paragraph("Report Summary").setFontSize(14).setBold());
        document.add(new Paragraph("Total Records: " + analyticsResult.getRecordCount()));
        document.add(new Paragraph("Execution Time: " + analyticsResult.getExecutionTimeMs() + " ms"));
        document.add(new Paragraph("\n"));
    }

    /**
     * Add data table with results
     */
    private void addDataTable(Document document, AnalyticsResult analyticsResult) {
        if (analyticsResult.getData().isEmpty()) {
            document.add(new Paragraph("No data available for the specified criteria."));
            return;
        }
        
        // Get column names from first row
        Set<String> columnNames = analyticsResult.getData().get(0).keySet();
        
        // Create table
        Table dataTable = new Table(columnNames.size());
        dataTable.setWidth(UnitValue.createPercentValue(100));
        
        // Add headers
        for (String columnName : columnNames) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(formatColumnName(columnName)).setBold())
                    .setTextAlignment(TextAlignment.CENTER);
            dataTable.addHeaderCell(headerCell);
        }
        
        // Add data rows
        for (Map<String, Object> row : analyticsResult.getData()) {
            for (String columnName : columnNames) {
                Object value = row.get(columnName);
                String displayValue = formatCellValue(value);
                
                Cell dataCell = new Cell()
                        .add(new Paragraph(displayValue))
                        .setTextAlignment(getTextAlignment(value));
                dataTable.addCell(dataCell);
            }
        }
        
        document.add(new Paragraph("Report Data").setFontSize(14).setBold());
        document.add(dataTable);
    }

    /**
     * Add report footer
     */
    private void addReportFooter(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Generated by DentistDSS Reporting Service")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(ReportRequest request, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.%s", request.getTemplateCode(), timestamp, extension);
    }

    /**
     * Format column name for display
     */
    private String formatColumnName(String columnName) {
        String formatted = columnName.replace("_", " ");
        // Capitalize first letter of each word
        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Format cell value for display
     */
    private String formatCellValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof Number) {
            // Format numbers with appropriate precision
            if (value instanceof Double || value instanceof Float) {
                return String.format("%.2f", ((Number) value).doubleValue());
            }
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        
        return String.valueOf(value);
    }

    /**
     * Get text alignment based on value type
     */
    private TextAlignment getTextAlignment(Object value) {
        if (value instanceof Number) {
            return TextAlignment.RIGHT;
        }
        return TextAlignment.LEFT;
    }
}
