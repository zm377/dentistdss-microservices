package com.dentistdss.workflow.model;

/**
 * Enumeration for workflow instance status
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
public enum WorkflowStatus {
    /**
     * Workflow has been created but not yet started
     */
    CREATED,
    
    /**
     * Workflow is currently running
     */
    RUNNING,
    
    /**
     * Workflow is paused and waiting for external input or approval
     */
    WAITING,
    
    /**
     * Workflow completed successfully
     */
    COMPLETED,
    
    /**
     * Workflow failed due to an error
     */
    FAILED,
    
    /**
     * Workflow was cancelled by user or system
     */
    CANCELLED,
    
    /**
     * Workflow timed out
     */
    TIMEOUT
}
