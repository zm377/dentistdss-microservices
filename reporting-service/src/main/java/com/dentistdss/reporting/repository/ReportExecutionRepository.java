package com.dentistdss.reporting.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.dentistdss.reporting.model.ReportExecution;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Report Executions
 * 
 * Manages report execution tracking and history in MongoDB.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ReportExecutionRepository extends MongoRepository<ReportExecution, String> {
    
    /**
     * Find executions by user
     */
    List<ReportExecution> findByRequestedByOrderByRequestedAtDesc(Long requestedBy, Pageable pageable);
    
    /**
     * Find executions by clinic
     */
    List<ReportExecution> findByClinicIdOrderByRequestedAtDesc(Long clinicId, Pageable pageable);
    
    /**
     * Find executions by template
     */
    List<ReportExecution> findByTemplateCodeOrderByRequestedAtDesc(String templateCode, Pageable pageable);
    
    /**
     * Find executions by status
     */
    List<ReportExecution> findByStatusOrderByRequestedAtDesc(
            ReportExecution.ExecutionStatus status, Pageable pageable);
    
    /**
     * Find scheduled executions
     */
    List<ReportExecution> findByScheduledOrderByRequestedAtDesc(Boolean scheduled, Pageable pageable);
    
    /**
     * Find executions in date range
     */
    List<ReportExecution> findByRequestedAtBetweenOrderByRequestedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find failed executions for retry
     */
    @Query("{ 'status': 'FAILED', 'requestedAt': { $gte: ?0 } }")
    List<ReportExecution> findFailedExecutionsAfter(LocalDateTime date);
    
    /**
     * Find long-running executions
     */
    @Query("{ 'status': 'RUNNING', 'startedAt': { $lte: ?0 } }")
    List<ReportExecution> findLongRunningExecutions(LocalDateTime cutoffTime);
    
    /**
     * Count executions by user and date range
     */
    long countByRequestedByAndRequestedAtBetween(
            Long requestedBy, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count executions by clinic and date range
     */
    long countByClinicIdAndRequestedAtBetween(
            Long clinicId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find executions with files to cleanup
     */
    @Query("{ 'generatedFiles.expiresAt': { $lte: ?0 } }")
    List<ReportExecution> findExecutionsWithExpiredFiles(LocalDateTime cutoffTime);
    
    /**
     * Get execution statistics
     */
    @Query(value = "{ 'requestedAt': { $gte: ?0, $lte: ?1 } }", 
           fields = "{ 'status': 1, 'templateCode': 1, 'metrics.totalExecutionTimeMs': 1 }")
    List<ReportExecution> findExecutionStatsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
