package com.dentistdss.genai.repository;

import com.dentistdss.genai.model.FAQ;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FAQ operations
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface FAQRepository extends MongoRepository<FAQ, String> {
    
    /**
     * Find active FAQs by category
     */
    List<FAQ> findByIsActiveTrueAndCategoryOrderByPriorityDescCreatedAtDesc(String category);
    
    /**
     * Find active FAQs for a specific clinic or global
     */
    @Query("{ 'isActive': true, $or: [ { 'clinicId': ?0 }, { 'clinicId': null } ] }")
    List<FAQ> findActiveByClinicIdOrGlobal(Long clinicId);
    
    /**
     * Full-text search in questions and answers
     */
    @Query("{ $text: { $search: ?0 }, 'isActive': true }")
    List<FAQ> findByTextSearch(String searchText);
    
    /**
     * Search FAQs by keywords
     */
    @Query("{ 'keywords': { $in: ?0 }, 'isActive': true }")
    List<FAQ> findByKeywordsIn(List<String> keywords);
    
    /**
     * Find FAQs by category and clinic
     */
    @Query("{ 'isActive': true, 'category': ?0, $or: [ { 'clinicId': ?1 }, { 'clinicId': null } ] }")
    List<FAQ> findByCategoryAndClinicIdOrGlobal(String category, Long clinicId);
    
    /**
     * Find top FAQs by view count
     */
    List<FAQ> findTop10ByIsActiveTrueOrderByViewCountDescCreatedAtDesc();
    
    /**
     * Find most helpful FAQs
     */
    List<FAQ> findTop10ByIsActiveTrueOrderByHelpfulCountDescCreatedAtDesc();
}
