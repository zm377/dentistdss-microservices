package com.dentistdss.genai.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.dentistdss.genai.model.PromptTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for managing prompt templates
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface PromptTemplateRepository extends ReactiveMongoRepository<PromptTemplate, String> {
    
    /**
     * Find active templates by agent type, ordered by priority
     */
    @Query("{ 'agentType': ?0, 'active': true }")
    Flux<PromptTemplate> findByAgentTypeAndActiveOrderByPriorityDesc(String agentType);
    
    /**
     * Find templates by agent type and supported role
     */
    @Query("{ 'agentType': ?0, 'active': true, 'supportedRoles': { $in: [?1] } }")
    Flux<PromptTemplate> findByAgentTypeAndSupportedRole(String agentType, String role);
    
    /**
     * Find templates by agent type and clinic ID
     */
    @Query("{ 'agentType': ?0, 'active': true, $or: [ { 'supportedClinics': { $size: 0 } }, { 'supportedClinics': { $in: [?1] } } ] }")
    Flux<PromptTemplate> findByAgentTypeAndClinicId(String agentType, String clinicId);
    
    /**
     * Find the best matching template for agent, role, and clinic
     */
    @Query("{ 'agentType': ?0, 'active': true, $or: [ { 'supportedRoles': { $size: 0 } }, { 'supportedRoles': { $in: [?1] } } ], $or: [ { 'supportedClinics': { $size: 0 } }, { 'supportedClinics': { $in: [?2] } } ] }")
    Flux<PromptTemplate> findBestMatchingTemplate(String agentType, String role, String clinicId);
    
    /**
     * Find template by name and agent type
     */
    Mono<PromptTemplate> findByTemplateNameAndAgentType(String templateName, String agentType);
    
    /**
     * Find all active templates
     */
    Flux<PromptTemplate> findByActiveTrue();
    
    /**
     * Find templates by version
     */
    Flux<PromptTemplate> findByVersion(String version);
}
