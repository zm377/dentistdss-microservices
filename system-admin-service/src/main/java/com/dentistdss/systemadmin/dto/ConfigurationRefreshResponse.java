package com.dentistdss.systemadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dentistdss.systemadmin.model.ConfigurationRefreshLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for Configuration Refresh operations
 * 
 * Comprehensive data transfer for configuration refresh administration responses
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationRefreshResponse {
    
    private Long id;
    private String refreshId;
    private ConfigurationRefreshLog.RefreshType refreshType;
    private String refreshTypeDisplayName;
    private String targetServices;
    private List<String> targetServicesList;
    private ConfigurationRefreshLog.RefreshStatus status;
    private String statusDisplayName;
    private String initiatedBy;
    private String reason;
    private String configurationChanges;
    private Integer totalServices;
    private Integer successfulServices;
    private Integer failedServices;
    private String detailedResults;
    private Map<String, ServiceRefreshResult> serviceResults;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Double successRate;
    private String summary;
    private String durationFormatted;
    
    /**
     * Create from entity
     */
    public static ConfigurationRefreshResponse fromEntity(ConfigurationRefreshLog entity) {
        ConfigurationRefreshResponseBuilder builder = ConfigurationRefreshResponse.builder()
                .id(entity.getId())
                .refreshId(entity.getRefreshId())
                .refreshType(entity.getRefreshType())
                .refreshTypeDisplayName(entity.getRefreshType().getDisplayName())
                .targetServices(entity.getTargetServices())
                .status(entity.getStatus())
                .statusDisplayName(entity.getStatus().getDisplayName())
                .initiatedBy(entity.getInitiatedBy())
                .reason(entity.getReason())
                .configurationChanges(entity.getConfigurationChanges())
                .totalServices(entity.getTotalServices())
                .successfulServices(entity.getSuccessfulServices())
                .failedServices(entity.getFailedServices())
                .detailedResults(entity.getDetailedResults())
                .errorMessage(entity.getErrorMessage())
                .durationMs(entity.getDurationMs())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .successRate(entity.getSuccessRate())
                .summary(entity.getSummary());
        
        // Parse target services list
        if (entity.getTargetServices() != null) {
            builder.targetServicesList(List.of(entity.getTargetServices().split(",")));
        }
        
        // Format duration
        if (entity.getDurationMs() != null) {
            builder.durationFormatted(formatDuration(entity.getDurationMs()));
        }
        
        return builder.build();
    }
    
    /**
     * Format duration in human-readable format
     */
    private static String formatDuration(Long durationMs) {
        if (durationMs == null) return "N/A";
        
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Check if the operation was successful
     */
    public boolean isSuccessful() {
        return status == ConfigurationRefreshLog.RefreshStatus.COMPLETED && 
               (failedServices == null || failedServices == 0);
    }
    
    /**
     * Check if the operation is still in progress
     */
    public boolean isInProgress() {
        return status == ConfigurationRefreshLog.RefreshStatus.IN_PROGRESS ||
               status == ConfigurationRefreshLog.RefreshStatus.INITIATED;
    }
    
    /**
     * Check if the operation is completed (success or failure)
     */
    public boolean isCompleted() {
        return status == ConfigurationRefreshLog.RefreshStatus.COMPLETED ||
               status == ConfigurationRefreshLog.RefreshStatus.FAILED ||
               status == ConfigurationRefreshLog.RefreshStatus.PARTIAL_SUCCESS ||
               status == ConfigurationRefreshLog.RefreshStatus.CANCELLED;
    }
    
    /**
     * Get status color for UI display
     */
    public String getStatusColor() {
        switch (status) {
            case COMPLETED:
                return "green";
            case FAILED:
                return "red";
            case PARTIAL_SUCCESS:
                return "orange";
            case IN_PROGRESS:
            case INITIATED:
                return "blue";
            case CANCELLED:
                return "gray";
            default:
                return "gray";
        }
    }
    
    /**
     * Get progress percentage
     */
    public int getProgressPercentage() {
        if (totalServices == null || totalServices == 0) return 0;
        
        int completed = (successfulServices != null ? successfulServices : 0) + 
                       (failedServices != null ? failedServices : 0);
        
        return Math.min(100, (completed * 100) / totalServices);
    }
    
    /**
     * Get operation result summary
     */
    public String getResultSummary() {
        if (isInProgress()) {
            return String.format("In progress... (%d%% complete)", getProgressPercentage());
        }
        
        if (isSuccessful()) {
            return String.format("Successfully refreshed %d service(s)", successfulServices);
        }
        
        if (status == ConfigurationRefreshLog.RefreshStatus.PARTIAL_SUCCESS) {
            return String.format("Partial success: %d successful, %d failed", 
                    successfulServices, failedServices);
        }
        
        if (status == ConfigurationRefreshLog.RefreshStatus.FAILED) {
            return String.format("Failed: %d service(s) could not be refreshed", failedServices);
        }
        
        return status.getDisplayName();
    }
    
    /**
     * Service refresh result details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceRefreshResult {
        private String serviceName;
        private boolean success;
        private String message;
        private Long durationMs;
        private LocalDateTime timestamp;
        
        public String getFormattedDuration() {
            return formatDuration(durationMs);
        }
    }
}
