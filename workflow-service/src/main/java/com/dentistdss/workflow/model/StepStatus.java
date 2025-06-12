package com.dentistdss.workflow.model;

/**
 * Enumeration for workflow step execution status
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
public enum StepStatus {
    /**
     * Step is pending execution
     */
    PENDING,
    
    /**
     * Step is currently being executed
     */
    RUNNING,
    
    /**
     * Step is waiting for approval or external input
     */
    WAITING_APPROVAL,
    
    /**
     * Step completed successfully
     */
    COMPLETED,
    
    /**
     * Step failed during execution
     */
    FAILED,
    
    /**
     * Step was skipped due to conditions
     */
    SKIPPED,
    
    /**
     * Step was cancelled
     */
    CANCELLED,
    
    /**
     * Step timed out
     */
    TIMEOUT
}
