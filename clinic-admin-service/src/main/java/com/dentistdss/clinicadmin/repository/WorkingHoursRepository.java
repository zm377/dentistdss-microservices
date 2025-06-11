package com.dentistdss.clinicadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicadmin.model.WorkingHours;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkingHours entity operations
 * 
 * Provides data access methods for clinic working hours management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {

    /**
     * Find all working hours for a clinic
     */
    List<WorkingHours> findByClinicIdOrderByDayOfWeekAscSpecificDateAsc(Long clinicId);

    /**
     * Find regular weekly working hours for a clinic
     */
    List<WorkingHours> findByClinicIdAndDayOfWeekIsNotNullOrderByDayOfWeekAsc(Long clinicId);

    /**
     * Find specific date working hours for a clinic
     */
    List<WorkingHours> findByClinicIdAndSpecificDateIsNotNullOrderBySpecificDateAsc(Long clinicId);

    /**
     * Find working hours for a specific day of week
     */
    Optional<WorkingHours> findByClinicIdAndDayOfWeek(Long clinicId, DayOfWeek dayOfWeek);

    /**
     * Find working hours for a specific date
     */
    Optional<WorkingHours> findByClinicIdAndSpecificDate(Long clinicId, LocalDate specificDate);

    /**
     * Find working hours for a date range (specific dates only)
     */
    @Query("SELECT wh FROM WorkingHours wh WHERE " +
           "wh.clinic.id = :clinicId AND " +
           "wh.specificDate BETWEEN :startDate AND :endDate " +
           "ORDER BY wh.specificDate ASC")
    List<WorkingHours> findByClinicIdAndDateRange(@Param("clinicId") Long clinicId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Find emergency hours for a clinic
     */
    List<WorkingHours> findByClinicIdAndIsEmergencyHoursTrueOrderBySpecificDateAsc(Long clinicId);

    /**
     * Find closed days for a clinic
     */
    List<WorkingHours> findByClinicIdAndIsClosedTrueOrderByDayOfWeekAscSpecificDateAsc(Long clinicId);

    /**
     * Check if clinic is open on a specific day of week
     */
    @Query("SELECT CASE WHEN COUNT(wh) > 0 THEN true ELSE false END FROM WorkingHours wh WHERE " +
           "wh.clinic.id = :clinicId AND " +
           "wh.dayOfWeek = :dayOfWeek AND " +
           "wh.isClosed = false")
    boolean isClinicOpenOnDayOfWeek(@Param("clinicId") Long clinicId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Check if clinic is open on a specific date
     */
    @Query("SELECT CASE WHEN COUNT(wh) > 0 THEN true ELSE false END FROM WorkingHours wh WHERE " +
           "wh.clinic.id = :clinicId AND " +
           "wh.specificDate = :date AND " +
           "wh.isClosed = false")
    boolean isClinicOpenOnSpecificDate(@Param("clinicId") Long clinicId, @Param("date") LocalDate date);

    /**
     * Find working hours with break times
     */
    @Query("SELECT wh FROM WorkingHours wh WHERE " +
           "wh.clinic.id = :clinicId AND " +
           "wh.breakStartTime IS NOT NULL AND " +
           "wh.breakEndTime IS NOT NULL " +
           "ORDER BY wh.dayOfWeek ASC, wh.specificDate ASC")
    List<WorkingHours> findByClinicIdWithBreakTimes(@Param("clinicId") Long clinicId);

    /**
     * Delete all working hours for a clinic
     */
    void deleteByClinicId(Long clinicId);

    /**
     * Delete working hours for a specific day of week
     */
    void deleteByClinicIdAndDayOfWeek(Long clinicId, DayOfWeek dayOfWeek);

    /**
     * Delete working hours for a specific date
     */
    void deleteByClinicIdAndSpecificDate(Long clinicId, LocalDate specificDate);

    /**
     * Count working hours for a clinic
     */
    long countByClinicId(Long clinicId);

    /**
     * Find upcoming special date working hours
     */
    @Query("SELECT wh FROM WorkingHours wh WHERE " +
           "wh.clinic.id = :clinicId AND " +
           "wh.specificDate >= CURRENT_DATE " +
           "ORDER BY wh.specificDate ASC")
    List<WorkingHours> findUpcomingSpecialHours(@Param("clinicId") Long clinicId);
}
