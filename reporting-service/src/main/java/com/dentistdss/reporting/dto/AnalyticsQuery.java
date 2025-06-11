package com.dentistdss.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Analytics Query DTO
 * 
 * Encapsulates analytical query parameters and metadata for execution.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsQuery {
    
    /**
     * Unique query identifier for caching and logging
     */
    private String queryId;
    
    /**
     * SQL query to execute
     */
    private String sql;
    
    /**
     * Query parameters
     */
    @Builder.Default
    private List<Object> parameters = List.of();
    
    /**
     * Whether this is a read-only query (can use replica)
     */
    @Builder.Default
    private Boolean readOnly = true;

    /**
     * Check if this is a read-only query
     */
    public boolean isReadOnly() {
        return readOnly != null ? readOnly : true;
    }
    
    /**
     * Query timeout in seconds
     */
    @Builder.Default
    private Integer timeoutSeconds = 300;
    
    /**
     * Additional query metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Generate cache key for this query
     */
    public String getCacheKey() {
        return String.format("%s:%s:%d", 
            queryId, 
            String.valueOf(parameters.hashCode()),
            System.currentTimeMillis() / (5 * 60 * 1000) // 5-minute cache buckets
        );
    }
}
