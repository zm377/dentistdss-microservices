package com.dentistdss.clinicadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicadmin.model.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Holiday entity operations
 * 
 * Provides data access methods for clinic holiday management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    /**
     * Find all holidays for a clinic
     */
    List<Holiday> findByClinicIdOrderByHolidayDateAsc(Long clinicId);

    /**
     * Find holiday by clinic and date
     */
    Optional<Holiday> findByClinicIdAndHolidayDate(Long clinicId, LocalDate holidayDate);

    /**
     * Find holidays for a date range
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "h.holidayDate BETWEEN :startDate AND :endDate " +
           "ORDER BY h.holidayDate ASC")
    List<Holiday> findByClinicIdAndDateRange(@Param("clinicId") Long clinicId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Find upcoming holidays for a clinic
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "h.holidayDate >= CURRENT_DATE " +
           "ORDER BY h.holidayDate ASC")
    List<Holiday> findUpcomingHolidays(@Param("clinicId") Long clinicId);

    /**
     * Find holidays by type
     */
    List<Holiday> findByClinicIdAndTypeOrderByHolidayDateAsc(Long clinicId, Holiday.HolidayType type);

    /**
     * Find recurring holidays
     */
    List<Holiday> findByClinicIdAndIsRecurringTrueOrderByHolidayDateAsc(Long clinicId);

    /**
     * Find full day closures
     */
    List<Holiday> findByClinicIdAndIsFullDayClosureTrueOrderByHolidayDateAsc(Long clinicId);

    /**
     * Find holidays with special hours
     */
    List<Holiday> findByClinicIdAndIsFullDayClosureFalseOrderByHolidayDateAsc(Long clinicId);

    /**
     * Check if a date is a holiday for a clinic
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND h.holidayDate = :date")
    boolean isHoliday(@Param("clinicId") Long clinicId, @Param("date") LocalDate date);

    /**
     * Find holidays affecting a specific date (including recurring holidays)
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "(h.holidayDate = :date OR " +
           "(h.isRecurring = true AND " +
           "MONTH(h.holidayDate) = MONTH(:date) AND " +
           "DAY(h.holidayDate) = DAY(:date)))")
    List<Holiday> findHolidaysAffectingDate(@Param("clinicId") Long clinicId, @Param("date") LocalDate date);

    /**
     * Find emergency closures
     */
    List<Holiday> findByClinicIdAndTypeOrderByHolidayDateDesc(Long clinicId, Holiday.HolidayType type);

    /**
     * Find holidays in current year
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "YEAR(h.holidayDate) = YEAR(CURRENT_DATE) " +
           "ORDER BY h.holidayDate ASC")
    List<Holiday> findCurrentYearHolidays(@Param("clinicId") Long clinicId);

    /**
     * Find holidays in specific year
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "YEAR(h.holidayDate) = :year " +
           "ORDER BY h.holidayDate ASC")
    List<Holiday> findHolidaysByYear(@Param("clinicId") Long clinicId, @Param("year") int year);

    /**
     * Count holidays for a clinic
     */
    long countByClinicId(Long clinicId);

    /**
     * Count upcoming holidays
     */
    @Query("SELECT COUNT(h) FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "h.holidayDate >= CURRENT_DATE")
    long countUpcomingHolidays(@Param("clinicId") Long clinicId);

    /**
     * Delete all holidays for a clinic
     */
    void deleteByClinicId(Long clinicId);

    /**
     * Find holidays with emergency contact information
     */
    @Query("SELECT h FROM Holiday h WHERE " +
           "h.clinic.id = :clinicId AND " +
           "h.emergencyContact IS NOT NULL AND " +
           "h.emergencyContact != '' " +
           "ORDER BY h.holidayDate ASC")
    List<Holiday> findHolidaysWithEmergencyContact(@Param("clinicId") Long clinicId);
}
