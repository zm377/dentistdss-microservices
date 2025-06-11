package press.mizhifei.dentist.systemadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Configuration Refresh Log Entity
 * 
 * Tracks configuration refresh operations across all microservices
 * for audit and monitoring purposes
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuration_refresh_logs", indexes = {
    @Index(name = "idx_refresh_type", columnList = "refreshType"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_initiated_by", columnList = "initiatedBy"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class ConfigurationRefreshLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique refresh operation ID
     */
    @NotBlank(message = "Refresh ID is required")
    @Column(unique = true, nullable = false, length = 100)
    private String refreshId;
    
    /**
     * Type of refresh operation
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Refresh type is required")
    @Column(nullable = false, length = 20)
    private RefreshType refreshType;
    
    /**
     * Target services (comma-separated for batch operations)
     */
    @Column(nullable = false, length = 500)
    private String targetServices;
    
    /**
     * Overall status of the refresh operation
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    @Column(nullable = false, length = 20)
    private RefreshStatus status;
    
    /**
     * User who initiated the refresh
     */
    @NotBlank(message = "Initiated by is required")
    @Column(nullable = false, length = 100)
    private String initiatedBy;
    
    /**
     * Reason for the refresh
     */
    @Column(length = 500)
    private String reason;
    
    /**
     * Configuration changes that triggered this refresh
     */
    @Column(columnDefinition = "TEXT")
    private String configurationChanges;
    
    /**
     * Total number of services targeted
     */
    private Integer totalServices;
    
    /**
     * Number of services successfully refreshed
     */
    @Builder.Default
    private Integer successfulServices = 0;
    
    /**
     * Number of services that failed to refresh
     */
    @Builder.Default
    private Integer failedServices = 0;
    
    /**
     * Detailed results for each service
     */
    @Column(columnDefinition = "TEXT")
    private String detailedResults;
    
    /**
     * Error message if the operation failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Duration of the refresh operation in milliseconds
     */
    private Long durationMs;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime completedAt;
    
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Refresh operation types
     */
    public enum RefreshType {
        ALL_SERVICES("All Services"),
        SINGLE_SERVICE("Single Service"),
        BATCH_SERVICES("Batch Services"),
        RATE_LIMIT_UPDATE("Rate Limit Update"),
        SYSTEM_PARAM_UPDATE("System Parameter Update");
        
        private final String displayName;
        
        RefreshType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Refresh operation status
     */
    public enum RefreshStatus {
        INITIATED("Initiated"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        PARTIAL_SUCCESS("Partial Success"),
        FAILED("Failed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        RefreshStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Mark the refresh operation as completed
     */
    public void markCompleted(RefreshStatus finalStatus, String results) {
        this.status = finalStatus;
        this.completedAt = LocalDateTime.now();
        this.detailedResults = results;
        if (createdAt != null) {
            this.durationMs = java.time.Duration.between(createdAt, completedAt).toMillis();
        }
    }
    
    /**
     * Update service counts
     */
    public void updateServiceCounts(int successful, int failed) {
        this.successfulServices = successful;
        this.failedServices = failed;
    }
    
    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (totalServices == null || totalServices == 0) {
            return 0.0;
        }
        return (double) successfulServices / totalServices * 100.0;
    }
    
    /**
     * Check if the operation was successful
     */
    public boolean isSuccessful() {
        return status == RefreshStatus.COMPLETED && failedServices == 0;
    }
    
    /**
     * Get operation summary
     */
    public String getSummary() {
        return String.format("%s refresh of %s - %s (%d/%d successful)", 
            refreshType.getDisplayName(),
            targetServices,
            status.getDisplayName(),
            successfulServices,
            totalServices);
    }
}
