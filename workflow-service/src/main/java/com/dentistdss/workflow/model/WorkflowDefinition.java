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
 * Workflow Definition Entity
 * 
 * Represents a configurable workflow template that defines the structure
 * and steps of a business process.
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
@Table(name = "workflow_definitions")
public class WorkflowDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_system_workflow", nullable = false)
    @Builder.Default
    private Boolean isSystemWorkflow = false;
    
    @Column(name = "timeout_minutes")
    private Integer timeoutMinutes;
    
    @Column(name = "max_retry_attempts")
    @Builder.Default
    private Integer maxRetryAttempts = 3;
    
    @Column(name = "auto_start", nullable = false)
    @Builder.Default
    private Boolean autoStart = false;
    
    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private Map<String, Object> configuration;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "input_schema", columnDefinition = "jsonb")
    private Map<String, Object> inputSchema;
    
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "output_schema", columnDefinition = "jsonb")
    private Map<String, Object> outputSchema;
    
    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStepDefinition> steps;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
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
