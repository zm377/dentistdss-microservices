package com.dentistdss.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Workflow Execution Entity
 * 
 * Represents the execution history and state of individual workflow steps.
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
@Table(name = "workflow_executions")
public class WorkflowExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id", nullable = false)
    private WorkflowInstance workflowInstance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_definition_id", nullable = false)
    private WorkflowStepDefinition stepDefinition;
    
    @Column(name = "step_name", nullable = false)
    private String stepName;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private StepType stepType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StepStatus status = StepStatus.PENDING;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "output_data", columnDefinition = "jsonb")
    private Map<String, Object> outputData;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "assigned_to")
    private Long assignedTo;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
