package com.dentistdss.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Workflow Instance Entity
 * 
 * Represents an active execution of a workflow definition.
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
@Table(name = "workflow_instances")
public class WorkflowInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;
    
    @Column(name = "instance_name")
    private String instanceName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.CREATED;
    
    @Column(name = "business_key")
    private String businessKey;
    
    @Column(name = "entity_type")
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "output_data", columnDefinition = "jsonb")
    private Map<String, Object> outputData;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;
    
    @Column(name = "current_step_order")
    private Integer currentStepOrder;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<WorkflowExecution> executions;
    
    @Column(name = "started_by")
    private Long startedBy;
    
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
