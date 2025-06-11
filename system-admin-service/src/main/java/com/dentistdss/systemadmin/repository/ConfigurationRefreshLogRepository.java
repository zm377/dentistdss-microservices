package com.dentistdss.systemadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.systemadmin.model.ConfigurationRefreshLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Configuration Refresh Log operations
 * 
 * Provides data access methods for tracking configuration refresh operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ConfigurationRefreshLogRepository extends JpaRepository<ConfigurationRefreshLog, Long> {

    /**
     * Find log by refresh ID
     */
    Optional<ConfigurationRefreshLog> findByRefreshId(String refreshId);

    /**
     * Find logs by refresh type
     */
    List<ConfigurationRefreshLog> findByRefreshTypeOrderByCreatedAtDesc(ConfigurationRefreshLog.RefreshType refreshType);

    /**
     * Find logs by status
     */
    List<ConfigurationRefreshLog> findByStatusOrderByCreatedAtDesc(ConfigurationRefreshLog.RefreshStatus status);

    /**
     * Find logs by user who initiated the refresh
     */
    List<ConfigurationRefreshLog> findByInitiatedByOrderByCreatedAtDesc(String initiatedBy);

    /**
     * Find logs for a specific service
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.targetServices LIKE CONCAT('%', :serviceName, '%') ORDER BY l.createdAt DESC")
    List<ConfigurationRefreshLog> findByTargetService(@Param("serviceName") String serviceName);

    /**
     * Find recent logs (within specified hours)
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.createdAt >= :since ORDER BY l.createdAt DESC")
    List<ConfigurationRefreshLog> findRecentLogs(@Param("since") LocalDateTime since);

    /**
     * Find failed refresh operations
     */
    List<ConfigurationRefreshLog> findByStatusInOrderByCreatedAtDesc(List<ConfigurationRefreshLog.RefreshStatus> statuses);



    /**
     * Find logs within date range
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.createdAt BETWEEN :startDate AND :endDate ORDER BY l.createdAt DESC")
    List<ConfigurationRefreshLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs with partial success
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.status = 'PARTIAL_SUCCESS' OR (l.failedServices > 0 AND l.successfulServices > 0) ORDER BY l.createdAt DESC")
    List<ConfigurationRefreshLog> findPartialSuccessLogs();

    /**
     * Find long-running operations (duration above threshold)
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.durationMs > :thresholdMs ORDER BY l.durationMs DESC")
    List<ConfigurationRefreshLog> findLongRunningOperations(@Param("thresholdMs") Long thresholdMs);

    /**
     * Count refresh operations by type
     */
    @Query("SELECT l.refreshType, COUNT(l) FROM ConfigurationRefreshLog l GROUP BY l.refreshType")
    List<Object[]> countByRefreshType();

    /**
     * Count refresh operations by status
     */
    @Query("SELECT l.status, COUNT(l) FROM ConfigurationRefreshLog l GROUP BY l.status")
    List<Object[]> countByStatus();

    /**
     * Count refresh operations by user
     */
    @Query("SELECT l.initiatedBy, COUNT(l) FROM ConfigurationRefreshLog l GROUP BY l.initiatedBy ORDER BY COUNT(l) DESC")
    List<Object[]> countByUser();

    /**
     * Get average duration by refresh type
     */
    @Query("SELECT l.refreshType, AVG(l.durationMs) FROM ConfigurationRefreshLog l WHERE l.durationMs IS NOT NULL GROUP BY l.refreshType")
    List<Object[]> getAverageDurationByType();

    /**
     * Get success rate by refresh type
     */
    @Query("SELECT l.refreshType, " +
           "COUNT(CASE WHEN l.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(l) as successRate " +
           "FROM ConfigurationRefreshLog l GROUP BY l.refreshType")
    List<Object[]> getSuccessRateByType();

    /**
     * Find most recent refresh for each service
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.id IN (" +
           "SELECT MAX(l2.id) FROM ConfigurationRefreshLog l2 " +
           "WHERE l2.refreshType = 'SINGLE_SERVICE' " +
           "GROUP BY l2.targetServices)")
    List<ConfigurationRefreshLog> findMostRecentRefreshPerService();

    /**
     * Find operations that took longer than average
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE " +
           "l.durationMs > (SELECT AVG(l2.durationMs) FROM ConfigurationRefreshLog l2 WHERE l2.durationMs IS NOT NULL) " +
           "ORDER BY l.durationMs DESC")
    List<ConfigurationRefreshLog> findSlowOperations();

    /**
     * Delete old logs older than specified date
     */
    @Query("DELETE FROM ConfigurationRefreshLog l WHERE l.createdAt < :cutoffDate")
    void deleteLogsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find logs with errors
     */
    @Query("SELECT l FROM ConfigurationRefreshLog l WHERE l.errorMessage IS NOT NULL ORDER BY l.createdAt DESC")
    List<ConfigurationRefreshLog> findLogsWithErrors();

    /**
     * Get refresh statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(l) as totalOperations, " +
           "COUNT(CASE WHEN l.status = 'COMPLETED' THEN 1 END) as successfulOperations, " +
           "COUNT(CASE WHEN l.status = 'FAILED' THEN 1 END) as failedOperations, " +
           "AVG(l.durationMs) as averageDuration " +
           "FROM ConfigurationRefreshLog l WHERE l.createdAt >= :since")
    Object[] getRefreshStatistics(@Param("since") LocalDateTime since);
}
