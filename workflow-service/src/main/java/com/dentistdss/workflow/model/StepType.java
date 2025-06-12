package com.dentistdss.workflow.model;

/**
 * Enumeration for workflow step types
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
public enum StepType {
    /**
     * Automated step that executes without human intervention
     */
    AUTOMATED,
    
    /**
     * Manual step that requires human approval or input
     */
    MANUAL_APPROVAL,
    
    /**
     * Service call step that invokes another microservice
     */
    SERVICE_CALL,
    
    /**
     * Notification step that sends notifications
     */
    NOTIFICATION,
    
    /**
     * Conditional step that evaluates conditions
     */
    CONDITIONAL,
    
    /**
     * Parallel step that can run concurrently with other steps
     */
    PARALLEL,
    
    /**
     * Wait step that pauses workflow for a specified duration
     */
    WAIT,
    
    /**
     * Script step that executes custom logic
     */
    SCRIPT
}
