package com.dentistdss.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Analytics Result DTO
 * 
 * Contains the results of analytical query execution with metadata.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResult {
    
    /**
     * Query identifier
     */
    private String queryId;
    
    /**
     * Query result data
     */
    private List<Map<String, Object>> data;
    
    /**
     * Number of records returned
     */
    private Integer recordCount;
    
    /**
     * Query execution time in milliseconds
     */
    private Long executionTimeMs;
    
    /**
     * When the query was executed
     */
    private LocalDateTime executedAt;
    
    /**
     * Whether the result was served from cache
     */
    @Builder.Default
    private Boolean cached = false;
    
    /**
     * Additional result metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Error information if query failed
     */
    private String errorMessage;
}
