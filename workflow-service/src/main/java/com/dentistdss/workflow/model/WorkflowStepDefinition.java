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
 * Workflow Step Definition Entity
 * 
 * Represents a step definition within a workflow template.
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
@Table(name = "workflow_step_definitions")
public class WorkflowStepDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;
    
    @Column(name = "step_name", nullable = false)
    private String stepName;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private StepType stepType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = true;
    
    @Column(name = "is_parallel", nullable = false)
    @Builder.Default
    private Boolean isParallel = false;
    
    @Column(name = "timeout_minutes")
    private Integer timeoutMinutes;
    
    @Column(name = "retry_attempts")
    @Builder.Default
    private Integer retryAttempts = 0;
    
    @Column(name = "condition_expression")
    private String conditionExpression;
    
    @Column(name = "approval_roles", columnDefinition = "text[]")
    private String[] approvalRoles;
    
    @Column(name = "service_endpoint")
    private String serviceEndpoint;
    
    @Column(name = "notification_template")
    private String notificationTemplate;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "step_configuration", columnDefinition = "jsonb")
    private Map<String, Object> stepConfiguration;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "input_mapping", columnDefinition = "jsonb")
    private Map<String, Object> inputMapping;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "output_mapping", columnDefinition = "jsonb")
    private Map<String, Object> outputMapping;
    
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
