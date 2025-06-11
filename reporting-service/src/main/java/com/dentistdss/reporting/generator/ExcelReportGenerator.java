package com.dentistdss.reporting.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * Excel Report Generator
 * 
 * Generates Excel reports with professional formatting, multiple sheets,
 * and data visualization capabilities using Apache POI.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelReportGenerator {

    @Value("${reporting.export.temp-directory:${java.io.tmpdir}/reporting}")
    private String tempDirectory;

    /**
     * Generate Excel report
     */
    public ReportExecution.GeneratedFile generateExcel(ReportRequest request, AnalyticsResult analyticsResult) {
        log.debug("Generating Excel report for template: {}", request.getTemplateCode());
        
        try {
            String fileName = generateFileName(request, "xlsx");
            String filePath = tempDirectory + File.separator + fileName;
            
            // Ensure directory exists
            new File(tempDirectory).mkdirs();
            
            // Create workbook
            Workbook workbook = new XSSFWorkbook();
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Add summary sheet
            createSummarySheet(workbook, request, analyticsResult, headerStyle, dataStyle);
            
            // Add data sheet
            createDataSheet(workbook, analyticsResult, headerStyle, dataStyle, numberStyle, dateStyle);
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            workbook.close();
            
            File file = new File(filePath);
            
            return ReportExecution.GeneratedFile.builder()
                    .format(ReportTemplate.ReportFormat.EXCEL)
                    .fileName(fileName)
                    .filePath(filePath)
                    .fileSize(file.length())
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .generatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error generating Excel report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    /**
     * Create summary sheet with report metadata
     */
    private void createSummarySheet(Workbook workbook, ReportRequest request, 
            AnalyticsResult analyticsResult, CellStyle headerStyle, CellStyle dataStyle) {
        
        Sheet summarySheet = workbook.createSheet("Summary");
        int rowNum = 0;
        
        // Report title
        Row titleRow = summarySheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(request.getTemplate().getName());
        titleCell.setCellStyle(headerStyle);
        
        // Generation info
        rowNum++; // Empty row
        Row genRow = summarySheet.createRow(rowNum++);
        genRow.createCell(0).setCellValue("Generated on:");
        genRow.createCell(1).setCellValue(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")));
        
        // Report metrics
        rowNum++; // Empty row
        Row metricsHeaderRow = summarySheet.createRow(rowNum++);
        Cell metricsHeaderCell = metricsHeaderRow.createCell(0);
        metricsHeaderCell.setCellValue("Report Metrics");
        metricsHeaderCell.setCellStyle(headerStyle);
        
        Row recordsRow = summarySheet.createRow(rowNum++);
        recordsRow.createCell(0).setCellValue("Total Records:");
        recordsRow.createCell(1).setCellValue(analyticsResult.getRecordCount());
        
        Row executionRow = summarySheet.createRow(rowNum++);
        executionRow.createCell(0).setCellValue("Execution Time (ms):");
        executionRow.createCell(1).setCellValue(analyticsResult.getExecutionTimeMs());
        
        // Parameters
        if (!request.getParameters().isEmpty()) {
            rowNum++; // Empty row
            Row paramHeaderRow = summarySheet.createRow(rowNum++);
            Cell paramHeaderCell = paramHeaderRow.createCell(0);
            paramHeaderCell.setCellValue("Parameters");
            paramHeaderCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
                Row paramRow = summarySheet.createRow(rowNum++);
                paramRow.createCell(0).setCellValue(entry.getKey());
                paramRow.createCell(1).setCellValue(String.valueOf(entry.getValue()));
            }
        }
        
        // Auto-size columns
        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);
    }

    /**
     * Create data sheet with query results
     */
    private void createDataSheet(Workbook workbook, AnalyticsResult analyticsResult,
            CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle, CellStyle dateStyle) {
        
        Sheet dataSheet = workbook.createSheet("Data");
        
        if (analyticsResult.getData().isEmpty()) {
            Row noDataRow = dataSheet.createRow(0);
            noDataRow.createCell(0).setCellValue("No data available for the specified criteria.");
            return;
        }
        
        // Get column names from first row
        Set<String> columnNames = analyticsResult.getData().get(0).keySet();
        String[] columns = columnNames.toArray(new String[0]);
        
        // Create header row
        Row headerRow = dataSheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(formatColumnName(columns[i]));
            headerCell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (Map<String, Object> row : analyticsResult.getData()) {
            Row dataRow = dataSheet.createRow(rowNum++);
            
            for (int i = 0; i < columns.length; i++) {
                Cell dataCell = dataRow.createCell(i);
                Object value = row.get(columns[i]);
                
                setCellValue(dataCell, value, dataStyle, numberStyle, dateStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < columns.length; i++) {
            dataSheet.autoSizeColumn(i);
        }
        
        // Freeze header row
        dataSheet.createFreezePane(0, 1);
    }

    /**
     * Set cell value with appropriate type and style
     */
    private void setCellValue(Cell cell, Object value, CellStyle dataStyle, 
            CellStyle numberStyle, CellStyle dateStyle) {
        
        if (value == null) {
            cell.setCellValue("");
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            cell.setCellStyle(numberStyle);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(String.valueOf(value));
            cell.setCellStyle(dataStyle);
        }
    }

    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * Create data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * Create number cell style
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    /**
     * Create date cell style
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
        return style;
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
}
