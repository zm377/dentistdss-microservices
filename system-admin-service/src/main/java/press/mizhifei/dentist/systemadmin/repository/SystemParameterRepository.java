package press.mizhifei.dentist.systemadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.systemadmin.model.SystemParameter;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for System Parameter operations
 * 
 * Provides comprehensive data access methods for system parameter administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    /**
     * Find parameter by unique key
     */
    Optional<SystemParameter> findByParameterKey(String parameterKey);

    /**
     * Check if parameter exists by key
     */
    boolean existsByParameterKey(String parameterKey);

    /**
     * Find all active parameters
     */
    List<SystemParameter> findByActiveTrueOrderByCategoryAscParameterKeyAsc();

    /**
     * Find parameters by category
     */
    List<SystemParameter> findByCategoryAndActiveTrueOrderByParameterKeyAsc(SystemParameter.ParameterCategory category);

    /**
     * Find parameters by service name
     */
    List<SystemParameter> findByServiceNameAndActiveTrueOrderByParameterKeyAsc(String serviceName);

    /**
     * Find global parameters (no specific service)
     */
    List<SystemParameter> findByServiceNameIsNullAndActiveTrueOrderByParameterKeyAsc();

    /**
     * Find parameters by environment
     */
    List<SystemParameter> findByEnvironmentAndActiveTrueOrderByParameterKeyAsc(String environment);

    /**
     * Find parameters by data type
     */
    List<SystemParameter> findByDataTypeAndActiveTrueOrderByParameterKeyAsc(SystemParameter.ParameterDataType dataType);

    /**
     * Find sensitive parameters
     */
    List<SystemParameter> findBySensitiveTrueAndActiveTrueOrderByParameterKeyAsc();

    /**
     * Find required parameters
     */
    List<SystemParameter> findByRequiredTrueAndActiveTrueOrderByParameterKeyAsc();

    /**
     * Find parameters requiring approval
     */
    List<SystemParameter> findByRequiresApprovalTrueAndActiveTrueOrderByParameterKeyAsc();

    /**
     * Find parameters for a specific service and environment
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "(p.serviceName IS NULL OR p.serviceName = :serviceName) AND " +
           "(p.environment IS NULL OR p.environment = :environment) " +
           "ORDER BY p.category ASC, p.parameterKey ASC")
    List<SystemParameter> findParametersForServiceAndEnvironment(
        @Param("serviceName") String serviceName,
        @Param("environment") String environment
    );

    /**
     * Find parameters by category and service
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "p.category = :category AND " +
           "(p.serviceName IS NULL OR p.serviceName = :serviceName) " +
           "ORDER BY p.parameterKey ASC")
    List<SystemParameter> findByCategoryAndService(
        @Param("category") SystemParameter.ParameterCategory category,
        @Param("serviceName") String serviceName
    );

    /**
     * Search parameters by key or name
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "(LOWER(p.parameterKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.parameterName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY p.parameterKey ASC")
    List<SystemParameter> searchParameters(@Param("searchTerm") String searchTerm);

    /**
     * Find parameters created by a specific user
     */
    List<SystemParameter> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find parameters updated by a specific user
     */
    List<SystemParameter> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);

    /**
     * Find parameters with null or empty values
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "(p.parameterValue IS NULL OR p.parameterValue = '') " +
           "ORDER BY p.required DESC, p.parameterKey ASC")
    List<SystemParameter> findParametersWithoutValues();

    /**
     * Find parameters using default values
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "p.parameterValue = p.defaultValue " +
           "ORDER BY p.parameterKey ASC")
    List<SystemParameter> findParametersUsingDefaults();

    /**
     * Count parameters by category
     */
    @Query("SELECT p.category, COUNT(p) FROM SystemParameter p WHERE p.active = true GROUP BY p.category")
    List<Object[]> countParametersByCategory();

    /**
     * Count parameters by service
     */
    @Query("SELECT COALESCE(p.serviceName, 'GLOBAL'), COUNT(p) FROM SystemParameter p WHERE p.active = true GROUP BY p.serviceName")
    List<Object[]> countParametersByService();

    /**
     * Count parameters by data type
     */
    @Query("SELECT p.dataType, COUNT(p) FROM SystemParameter p WHERE p.active = true GROUP BY p.dataType")
    List<Object[]> countParametersByDataType();

    /**
     * Find recently updated parameters
     */
    @Query("SELECT p FROM SystemParameter p WHERE p.updatedAt >= :since ORDER BY p.updatedAt DESC")
    List<SystemParameter> findRecentlyUpdated(@Param("since") java.time.LocalDateTime since);

    /**
     * Find parameters by version
     */
    List<SystemParameter> findByVersionGreaterThanOrderByUpdatedAtDesc(Integer version);

    /**
     * Find parameters with validation patterns
     */
    @Query("SELECT p FROM SystemParameter p WHERE p.active = true AND p.validationPattern IS NOT NULL ORDER BY p.parameterKey ASC")
    List<SystemParameter> findParametersWithValidation();

    /**
     * Find parameters with value constraints
     */
    @Query("SELECT p FROM SystemParameter p WHERE " +
           "p.active = true AND " +
           "(p.minValue IS NOT NULL OR p.maxValue IS NOT NULL OR p.allowedValues IS NOT NULL) " +
           "ORDER BY p.parameterKey ASC")
    List<SystemParameter> findParametersWithConstraints();

    /**
     * Delete inactive parameters older than specified date
     */
    @Query("DELETE FROM SystemParameter p WHERE p.active = false AND p.updatedAt < :cutoffDate")
    void deleteInactiveParametersOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
