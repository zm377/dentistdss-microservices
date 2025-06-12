package com.dentistdss.workflow.repository;

import com.dentistdss.workflow.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkflowDefinition entities
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    
    /**
     * Find workflow definition by name
     */
    Optional<WorkflowDefinition> findByName(String name);
    
    /**
     * Find workflow definition by name and version
     */
    Optional<WorkflowDefinition> findByNameAndVersion(String name, Integer version);
    
    /**
     * Find all active workflow definitions
     */
    List<WorkflowDefinition> findByIsActiveTrue();
    
    /**
     * Find workflow definitions by category
     */
    List<WorkflowDefinition> findByCategoryAndIsActiveTrue(String category);
    
    /**
     * Find system workflow definitions
     */
    List<WorkflowDefinition> findByIsSystemWorkflowTrueAndIsActiveTrue();
    
    /**
     * Find the latest version of a workflow by name
     */
    @Query("SELECT w FROM WorkflowDefinition w WHERE w.name = :name AND w.isActive = true ORDER BY w.version DESC")
    Optional<WorkflowDefinition> findLatestVersionByName(@Param("name") String name);
    
    /**
     * Find all versions of a workflow by name
     */
    List<WorkflowDefinition> findByNameOrderByVersionDesc(String name);
    
    /**
     * Check if workflow name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if workflow name and version combination exists
     */
    boolean existsByNameAndVersion(String name, Integer version);
}
