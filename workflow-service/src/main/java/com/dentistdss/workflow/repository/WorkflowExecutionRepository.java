package com.dentistdss.workflow.repository;

import com.dentistdss.workflow.model.StepStatus;
import com.dentistdss.workflow.model.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkflowExecution entities
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, Long> {
    
    /**
     * Find executions by workflow instance ID
     */
    List<WorkflowExecution> findByWorkflowInstanceIdOrderByStepOrderAsc(Long workflowInstanceId);
    
    /**
     * Find executions by workflow instance ID and status
     */
    List<WorkflowExecution> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, StepStatus status);
    
    /**
     * Find executions assigned to a specific user
     */
    List<WorkflowExecution> findByAssignedToAndStatus(Long assignedTo, StepStatus status);
    
    /**
     * Find pending approval executions
     */
    List<WorkflowExecution> findByStatusOrderByCreatedAtAsc(StepStatus status);
    
    /**
     * Find the current executing step for a workflow instance
     */
    @Query("SELECT e FROM WorkflowExecution e WHERE e.workflowInstance.id = :instanceId AND e.status IN ('RUNNING', 'WAITING_APPROVAL') ORDER BY e.stepOrder ASC")
    Optional<WorkflowExecution> findCurrentExecutingStep(@Param("instanceId") Long instanceId);
    
    /**
     * Find the next pending step for a workflow instance
     */
    @Query("SELECT e FROM WorkflowExecution e WHERE e.workflowInstance.id = :instanceId AND e.status = 'PENDING' ORDER BY e.stepOrder ASC")
    Optional<WorkflowExecution> findNextPendingStep(@Param("instanceId") Long instanceId);
    
    /**
     * Find executions that have timed out
     */
    @Query("SELECT e FROM WorkflowExecution e WHERE e.status IN ('RUNNING', 'WAITING_APPROVAL') AND e.timeoutAt < :currentTime")
    List<WorkflowExecution> findTimedOutExecutions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find failed executions that can be retried
     */
    @Query("SELECT e FROM WorkflowExecution e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries")
    List<WorkflowExecution> findRetryableExecutions(@Param("maxRetries") Integer maxRetries);
    
    /**
     * Count executions by status for a workflow instance
     */
    long countByWorkflowInstanceIdAndStatus(Long workflowInstanceId, StepStatus status);
    
    /**
     * Find executions by step name and status
     */
    List<WorkflowExecution> findByStepNameAndStatus(String stepName, StepStatus status);
    
    /**
     * Find all executions for a workflow instance with step definitions
     */
    @Query("SELECT e FROM WorkflowExecution e JOIN FETCH e.stepDefinition WHERE e.workflowInstance.id = :instanceId ORDER BY e.stepOrder ASC")
    List<WorkflowExecution> findByWorkflowInstanceIdWithStepDefinitions(@Param("instanceId") Long instanceId);
}
