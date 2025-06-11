package com.dentistdss.clinicadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicadmin.model.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Service entity operations
 * 
 * Provides data access methods for clinic service management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {

    /**
     * Find all services for a clinic
     */
    List<Service> findByClinicIdOrderByNameAsc(Long clinicId);

    /**
     * Find active services for a clinic
     */
    List<Service> findByClinicIdAndIsActiveTrueOrderByNameAsc(Long clinicId);

    /**
     * Find services by category for a clinic
     */
    List<Service> findByClinicIdAndCategoryIgnoreCaseOrderByNameAsc(Long clinicId, String category);

    /**
     * Find active services by category for a clinic
     */
    List<Service> findByClinicIdAndCategoryIgnoreCaseAndIsActiveTrueOrderByNameAsc(Long clinicId, String category);

    /**
     * Find service by name for a clinic
     */
    Optional<Service> findByClinicIdAndNameIgnoreCase(Long clinicId, String name);

    /**
     * Check if service exists by name for a clinic
     */
    boolean existsByClinicIdAndNameIgnoreCase(Long clinicId, String name);

    /**
     * Find services by price range
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.price BETWEEN :minPrice AND :maxPrice AND " +
           "s.isActive = true " +
           "ORDER BY s.price ASC")
    List<Service> findByClinicIdAndPriceRange(@Param("clinicId") Long clinicId,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find services by duration range
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.durationMinutes BETWEEN :minDuration AND :maxDuration AND " +
           "s.isActive = true " +
           "ORDER BY s.durationMinutes ASC")
    List<Service> findByClinicIdAndDurationRange(@Param("clinicId") Long clinicId,
                                               @Param("minDuration") Integer minDuration,
                                               @Param("maxDuration") Integer maxDuration);

    /**
     * Search services by name or description
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "s.isActive = true " +
           "ORDER BY s.name ASC")
    List<Service> searchActiveServices(@Param("clinicId") Long clinicId, @Param("searchTerm") String searchTerm);

    /**
     * Find all distinct categories for a clinic
     */
    @Query("SELECT DISTINCT s.category FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.category IS NOT NULL AND " +
           "s.isActive = true " +
           "ORDER BY s.category ASC")
    List<String> findDistinctCategoriesByClinicId(@Param("clinicId") Long clinicId);

    /**
     * Count services for a clinic
     */
    long countByClinicId(Long clinicId);

    /**
     * Count active services for a clinic
     */
    long countByClinicIdAndIsActiveTrue(Long clinicId);

    /**
     * Find most expensive services for a clinic
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.isActive = true " +
           "ORDER BY s.price DESC")
    List<Service> findMostExpensiveServices(@Param("clinicId") Long clinicId);

    /**
     * Find least expensive services for a clinic
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.isActive = true " +
           "ORDER BY s.price ASC")
    List<Service> findLeastExpensiveServices(@Param("clinicId") Long clinicId);

    /**
     * Find longest duration services for a clinic
     */
    @Query("SELECT s FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.isActive = true " +
           "ORDER BY s.durationMinutes DESC")
    List<Service> findLongestDurationServices(@Param("clinicId") Long clinicId);

    /**
     * Delete all services for a clinic
     */
    void deleteByClinicId(Long clinicId);

    /**
     * Get average price of services for a clinic
     */
    @Query("SELECT AVG(s.price) FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.isActive = true AND " +
           "s.price IS NOT NULL")
    BigDecimal getAveragePriceByClinicId(@Param("clinicId") Long clinicId);

    /**
     * Get average duration of services for a clinic
     */
    @Query("SELECT AVG(s.durationMinutes) FROM Service s WHERE " +
           "s.clinicId = :clinicId AND " +
           "s.isActive = true")
    Double getAverageDurationByClinicId(@Param("clinicId") Long clinicId);
}
