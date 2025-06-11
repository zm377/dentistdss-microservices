package press.mizhifei.dentist.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.system.model.RateLimitConfig;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Rate Limit Configuration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface RateLimitConfigRepository extends JpaRepository<RateLimitConfig, Long> {
    
    /**
     * Find configuration by name
     */
    Optional<RateLimitConfig> findByConfigName(String configName);
    
    /**
     * Find all active configurations
     */
    List<RateLimitConfig> findByActiveTrue();
    
    /**
     * Find configurations for a specific service
     */
    List<RateLimitConfig> findByServiceNameAndActiveTrue(String serviceName);
    
    /**
     * Find configurations matching endpoint pattern
     */
    @Query("SELECT r FROM RateLimitConfig r WHERE r.active = true AND :endpoint LIKE CONCAT(r.endpointPattern, '%') ORDER BY r.priority DESC")
    List<RateLimitConfig> findMatchingConfigurations(@Param("endpoint") String endpoint);
    
    /**
     * Find the best matching configuration for a specific context
     */
    @Query("""
        SELECT r FROM RateLimitConfig r 
        WHERE r.active = true 
        AND :endpoint LIKE CONCAT(r.endpointPattern, '%')
        AND (r.userRole IS NULL OR r.userRole = :userRole)
        AND (r.clinicId IS NULL OR r.clinicId = :clinicId)
        ORDER BY r.priority DESC, 
                 CASE WHEN r.userRole IS NOT NULL THEN 1 ELSE 0 END DESC,
                 CASE WHEN r.clinicId IS NOT NULL THEN 1 ELSE 0 END DESC
        """)
    List<RateLimitConfig> findBestMatchingConfiguration(
        @Param("endpoint") String endpoint,
        @Param("userRole") String userRole,
        @Param("clinicId") Long clinicId
    );
}
