package com.dentistdss.workflow.repository;

import com.dentistdss.workflow.model.WorkflowInstance;
import com.dentistdss.workflow.model.WorkflowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkflowInstance entities
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    
    /**
     * Find workflow instance by business key
     */
    Optional<WorkflowInstance> findByBusinessKey(String businessKey);
    
    /**
     * Find workflow instances by status
     */
    List<WorkflowInstance> findByStatus(WorkflowStatus status);
    
    /**
     * Find workflow instances by entity type and entity ID
     */
    List<WorkflowInstance> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    /**
     * Find workflow instances by workflow definition ID
     */
    List<WorkflowInstance> findByWorkflowDefinitionId(Long workflowDefinitionId);
    
    /**
     * Find workflow instances started by a specific user
     */
    List<WorkflowInstance> findByStartedBy(Long startedBy);
    
    /**
     * Find running workflow instances
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.status IN ('RUNNING', 'WAITING')")
    List<WorkflowInstance> findActiveInstances();
    
    /**
     * Find workflow instances that have timed out
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.status IN ('RUNNING', 'WAITING') AND w.timeoutAt < :currentTime")
    List<WorkflowInstance> findTimedOutInstances(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find workflow instances by status with pagination
     */
    Page<WorkflowInstance> findByStatus(WorkflowStatus status, Pageable pageable);
    
    /**
     * Find workflow instances by workflow definition name
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.workflowDefinition.name = :workflowName")
    List<WorkflowInstance> findByWorkflowDefinitionName(@Param("workflowName") String workflowName);
    
    /**
     * Find workflow instances created within a date range
     */
    @Query("SELECT w FROM WorkflowInstance w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<WorkflowInstance> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count workflow instances by status
     */
    long countByStatus(WorkflowStatus status);
    
    /**
     * Count workflow instances by workflow definition
     */
    long countByWorkflowDefinitionId(Long workflowDefinitionId);
}
