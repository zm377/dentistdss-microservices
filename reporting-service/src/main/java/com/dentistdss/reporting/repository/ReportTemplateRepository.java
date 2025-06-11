package com.dentistdss.reporting.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.dentistdss.reporting.model.ReportTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Report Templates
 * 
 * Manages report template persistence and retrieval from MongoDB.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ReportTemplateRepository extends MongoRepository<ReportTemplate, String> {
    
    /**
     * Find template by code and active status
     */
    Optional<ReportTemplate> findByTemplateCodeAndActive(String templateCode, Boolean active);
    
    /**
     * Find all active templates
     */
    List<ReportTemplate> findByActiveOrderByName(Boolean active);
    
    /**
     * Find templates by category
     */
    List<ReportTemplate> findByCategoryAndActiveOrderByName(
            ReportTemplate.ReportCategory category, Boolean active);
    
    /**
     * Find templates by type
     */
    List<ReportTemplate> findByTypeAndActiveOrderByName(
            ReportTemplate.ReportType type, Boolean active);
    
    /**
     * Find templates accessible to user roles
     */
    @Query("{ 'active': ?0, 'allowedRoles': { $in: ?1 } }")
    List<ReportTemplate> findByActiveAndAllowedRolesIn(Boolean active, List<String> roles);
    
    /**
     * Find templates by creator
     */
    List<ReportTemplate> findByCreatedByAndActiveOrderByCreatedAtDesc(Long createdBy, Boolean active);
    
    /**
     * Check if template code exists
     */
    boolean existsByTemplateCode(String templateCode);
    
    /**
     * Find templates modified after date
     */
    @Query("{ 'modifiedAt': { $gte: ?0 }, 'active': true }")
    List<ReportTemplate> findModifiedAfter(java.time.LocalDateTime date);
}
