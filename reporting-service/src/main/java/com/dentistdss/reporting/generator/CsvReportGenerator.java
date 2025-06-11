package com.dentistdss.reporting.generator;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.dentistdss.reporting.dto.AnalyticsResult;
import com.dentistdss.reporting.dto.ReportRequest;
import com.dentistdss.reporting.model.ReportExecution;
import com.dentistdss.reporting.model.ReportTemplate;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSV Report Generator
 * 
 * Generates CSV reports optimized for data exchange and import into other systems.
 * Uses OpenCSV library for robust CSV generation with proper escaping.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvReportGenerator {

    @Value("${reporting.export.temp-directory:${java.io.tmpdir}/reporting}")
    private String tempDirectory;

    /**
     * Generate CSV report
     */
    public ReportExecution.GeneratedFile generateCsv(ReportRequest request, AnalyticsResult analyticsResult) {
        log.debug("Generating CSV report for template: {}", request.getTemplateCode());
        
        try {
            String fileName = generateFileName(request, "csv");
            String filePath = tempDirectory + File.separator + fileName;
            
            // Ensure directory exists
            new File(tempDirectory).mkdirs();
            
            // Create CSV writer
            try (FileWriter fileWriter = new FileWriter(filePath);
                 CSVWriter csvWriter = new CSVWriter(fileWriter)) {
                
                // Write metadata header (optional)
                writeMetadataHeader(csvWriter, request, analyticsResult);
                
                // Write data
                writeDataRows(csvWriter, analyticsResult);
            }
            
            File file = new File(filePath);
            
            return ReportExecution.GeneratedFile.builder()
                    .format(ReportTemplate.ReportFormat.CSV)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileSize(file.length())
                    .contentType("text/csv")
                    .generatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating CSV report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    /**
     * Write metadata header (commented lines)
     */
    private void writeMetadataHeader(CSVWriter csvWriter, ReportRequest request, AnalyticsResult analyticsResult) {
        // Report title
        csvWriter.writeNext(new String[]{"# " + request.getTemplate().getName()});
        
        // Generation info
        csvWriter.writeNext(new String[]{"# Generated on: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))});
        
        // Report metrics
        csvWriter.writeNext(new String[]{"# Total Records: " + analyticsResult.getRecordCount()});
        csvWriter.writeNext(new String[]{"# Execution Time: " + analyticsResult.getExecutionTimeMs() + " ms"});
        
        // Parameters
        if (!request.getParameters().isEmpty()) {
            csvWriter.writeNext(new String[]{"# Parameters:"});
            for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                csvWriter.writeNext(new String[]{"# " + entry.getKey() + ": " + entry.getValue()});
            }
        }
        
        // Empty line separator
        csvWriter.writeNext(new String[]{""});
    }

    /**
     * Write data rows
     */
    private void writeDataRows(CSVWriter csvWriter, AnalyticsResult analyticsResult) {
        if (analyticsResult.getData().isEmpty()) {
            csvWriter.writeNext(new String[]{"No data available for the specified criteria."});
            return;
        }
        
        // Get column names from first row
        Set<String> columnNames = analyticsResult.getData().get(0).keySet();
        String[] columns = columnNames.toArray(new String[0]);
        
        // Write header row
        String[] headers = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            headers[i] = formatColumnName(columns[i]);
        }
        csvWriter.writeNext(headers);
        
        // Write data rows
        for (Map<String, Object> row : analyticsResult.getData()) {
            String[] values = new String[columns.length];
            
            for (int i = 0; i < columns.length; i++) {
                Object value = row.get(columns[i]);
                values[i] = formatCellValue(value);
            }
            
            csvWriter.writeNext(values);
        }
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
     * Format cell value for CSV output
     */
    private String formatCellValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof Number) {
            // Format numbers without thousand separators for CSV
            if (value instanceof Double || value instanceof Float) {
                return String.format("%.2f", ((Number) value).doubleValue());
            }
            return String.valueOf(value);
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        return String.valueOf(value);
    }
}
