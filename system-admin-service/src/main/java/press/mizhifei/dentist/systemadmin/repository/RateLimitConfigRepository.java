package press.mizhifei.dentist.systemadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.systemadmin.model.RateLimitConfig;

import java.util.List;
import java.util.Optional;

/**
 * Enhanced Repository interface for Rate Limit Configuration operations
 * 
 * Provides comprehensive data access methods for rate limiting administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfig, Long> {

    /**
     * Find configuration by unique name
     */
    Optional<RateLimitConfig> findByConfigName(String configName);

    /**
     * Check if configuration exists by name
     */
    boolean existsByConfigName(String configName);

    /**
     * Find all active configurations
     */
    List<RateLimitConfig> findByActiveTrueOrderByPriorityDescCreatedAtAsc();

    /**
     * Find configurations by service name
     */
    List<RateLimitConfig> findByServiceNameAndActiveTrueOrderByPriorityDescCreatedAtAsc(String serviceName);

    /**
     * Find configurations by user role
     */
    List<RateLimitConfig> findByUserRoleAndActiveTrueOrderByPriorityDescCreatedAtAsc(String userRole);

    /**
     * Find configurations by clinic ID
     */
    List<RateLimitConfig> findByClinicIdAndActiveTrueOrderByPriorityDescCreatedAtAsc(Long clinicId);

    /**
     * Find configurations by category
     */
    List<RateLimitConfig> findByCategoryAndActiveTrueOrderByPriorityDescCreatedAtAsc(String category);

    /**
     * Find configurations by environment
     */
    List<RateLimitConfig> findByEnvironmentAndActiveTrueOrderByPriorityDescCreatedAtAsc(String environment);

    /**
     * Find configurations by limit type
     */
    List<RateLimitConfig> findByLimitTypeAndActiveTrueOrderByPriorityDescCreatedAtAsc(RateLimitConfig.RateLimitType limitType);

    /**
     * Find matching configurations for a specific request context
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE " +
           "r.active = true AND " +
           "r.serviceName = :serviceName AND " +
           "(:userRole IS NULL OR r.userRole IS NULL OR r.userRole = :userRole) AND " +
           "(:clinicId IS NULL OR r.clinicId IS NULL OR r.clinicId = :clinicId) AND " +
           "(:environment IS NULL OR r.environment IS NULL OR r.environment = :environment) " +
           "ORDER BY r.priority DESC, r.createdAt ASC")
    List<RateLimitConfig> findMatchingConfigurations(
        @Param("serviceName") String serviceName,
        @Param("userRole") String userRole,
        @Param("clinicId") Long clinicId,
        @Param("environment") String environment
    );

    /**
     * Find the best matching configuration for a specific context
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE " +
           "r.active = true AND " +
           "r.serviceName = :serviceName AND " +
           "(:userRole IS NULL OR r.userRole IS NULL OR r.userRole = :userRole) AND " +
           "(:clinicId IS NULL OR r.clinicId IS NULL OR r.clinicId = :clinicId) AND " +
           "(:environment IS NULL OR r.environment IS NULL OR r.environment = :environment) " +
           "ORDER BY " +
           "CASE WHEN r.userRole = :userRole THEN 4 ELSE 0 END + " +
           "CASE WHEN r.clinicId = :clinicId THEN 2 ELSE 0 END + " +
           "CASE WHEN r.environment = :environment THEN 1 ELSE 0 END DESC, " +
           "r.priority DESC, r.createdAt ASC")
    Optional<RateLimitConfig> findBestMatchingConfiguration(
        @Param("serviceName") String serviceName,
        @Param("userRole") String userRole,
        @Param("clinicId") Long clinicId,
        @Param("environment") String environment
    );

    /**
     * Find configurations created by a specific user
     */
    List<RateLimitConfig> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find configurations updated by a specific user
     */
    List<RateLimitConfig> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);

    /**
     * Find configurations with high priority (above threshold)
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE r.active = true AND r.priority >= :threshold ORDER BY r.priority DESC")
    List<RateLimitConfig> findHighPriorityConfigurations(@Param("threshold") Integer threshold);

    /**
     * Find configurations that need approval
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE r.active = true AND r.maxRequests > :threshold")
    List<RateLimitConfig> findConfigurationsRequiringApproval(@Param("threshold") Long threshold);

    /**
     * Count configurations by service
     */
    @Query("SELECT r.serviceName, COUNT(r) FROM RateLimitConfig r WHERE r.active = true GROUP BY r.serviceName")
    List<Object[]> countConfigurationsByService();

    /**
     * Count configurations by category
     */
    @Query("SELECT r.category, COUNT(r) FROM RateLimitConfig r WHERE r.active = true GROUP BY r.category")
    List<Object[]> countConfigurationsByCategory();

    /**
     * Find configurations with overlapping rules (potential conflicts)
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE " +
           "r.active = true AND " +
           "r.serviceName = :serviceName AND " +
           "r.endpointPattern = :endpointPattern AND " +
           "r.id != :excludeId")
    List<RateLimitConfig> findPotentialConflicts(
        @Param("serviceName") String serviceName,
        @Param("endpointPattern") String endpointPattern,
        @Param("excludeId") Long excludeId
    );

    /**
     * Find recently updated configurations
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE r.updatedAt >= :since ORDER BY r.updatedAt DESC")
    List<RateLimitConfig> findRecentlyUpdated(@Param("since") java.time.LocalDateTime since);

    /**
     * Find configurations by version
     */
    List<RateLimitConfig> findByVersionGreaterThanOrderByUpdatedAtDesc(Integer version);

    /**
     * Delete inactive configurations older than specified date
     */
    @Query("DELETE FROM RateLimitConfig r WHERE r.active = false AND r.updatedAt < :cutoffDate")
    void deleteInactiveConfigurationsOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
