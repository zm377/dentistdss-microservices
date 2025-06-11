package press.mizhifei.dentist.clinicadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinicadmin.model.Clinic;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Clinic entity operations
 * 
 * Provides comprehensive data access methods for clinic administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    /**
     * Find all enabled clinics
     */
    List<Clinic> findByEnabledTrueOrderByNameAsc();

    /**
     * Find all approved clinics
     */
    List<Clinic> findByApprovedTrueOrderByNameAsc();

    /**
     * Find all enabled and approved clinics
     */
    List<Clinic> findByEnabledTrueAndApprovedTrueOrderByNameAsc();

    /**
     * Find clinic by name (case-insensitive)
     */
    Optional<Clinic> findByNameIgnoreCase(String name);

    /**
     * Check if clinic exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find clinics by city (case-insensitive)
     */
    List<Clinic> findByCityIgnoreCaseOrderByNameAsc(String city);

    /**
     * Find clinics by state (case-insensitive)
     */
    List<Clinic> findByStateIgnoreCaseOrderByNameAsc(String state);

    /**
     * Find clinics by country (case-insensitive)
     */
    List<Clinic> findByCountryIgnoreCaseOrderByNameAsc(String country);

    /**
     * Search clinics by name, city, or state (case-insensitive)
     */
    @Query("SELECT c FROM Clinic c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.state) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "c.enabled = true AND c.approved = true " +
           "ORDER BY c.name ASC")
    List<Clinic> searchEnabledClinics(@Param("searchTerm") String searchTerm);

    /**
     * Find clinics by location (city and state)
     */
    @Query("SELECT c FROM Clinic c WHERE " +
           "LOWER(c.city) = LOWER(:city) AND " +
           "LOWER(c.state) = LOWER(:state) AND " +
           "c.enabled = true AND c.approved = true " +
           "ORDER BY c.name ASC")
    List<Clinic> findByLocationAndEnabled(@Param("city") String city, @Param("state") String state);

    /**
     * Find clinics within a geographic area (simplified - could be enhanced with spatial queries)
     */
    @Query("SELECT c FROM Clinic c WHERE " +
           "LOWER(c.city) IN :cities AND " +
           "c.enabled = true AND c.approved = true " +
           "ORDER BY c.name ASC")
    List<Clinic> findByMultipleCitiesAndEnabled(@Param("cities") List<String> cities);

    /**
     * Count enabled clinics
     */
    long countByEnabledTrue();

    /**
     * Count approved clinics
     */
    long countByApprovedTrue();

    /**
     * Count enabled and approved clinics
     */
    long countByEnabledTrueAndApprovedTrue();

    /**
     * Find clinics pending approval
     */
    List<Clinic> findByApprovedFalseOrderByCreatedAtAsc();

    /**
     * Find recently created clinics
     */
    @Query("SELECT c FROM Clinic c WHERE " +
           "c.createdAt >= CURRENT_DATE - :days " +
           "ORDER BY c.createdAt DESC")
    List<Clinic> findRecentlyCreated(@Param("days") int days);
}
