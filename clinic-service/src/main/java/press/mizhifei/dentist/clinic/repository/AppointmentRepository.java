package press.mizhifei.dentist.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinic.model.Appointment;
import press.mizhifei.dentist.clinic.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);
    
    List<Appointment> findByDentistIdAndAppointmentDateOrderByStartTime(Long dentistId, LocalDate date);
    
    List<Appointment> findByClinicIdAndAppointmentDateOrderByStartTime(Long clinicId, LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.dentistId = :dentistId " +
           "AND a.appointmentDate = :date " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingAppointments(@Param("dentistId") Long dentistId,
                                                   @Param("date") LocalDate date,
                                                   @Param("startTime") LocalTime startTime,
                                                   @Param("endTime") LocalTime endTime);
    
    List<Appointment> findByStatusAndAppointmentDateBetween(AppointmentStatus status, 
                                                             LocalDate startDate, 
                                                             LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :tomorrow " +
           "AND a.status = 'CONFIRMED'")
    List<Appointment> findConfirmedAppointmentsForDate(@Param("tomorrow") LocalDate tomorrow);

    /**
     * Find the most recent completed appointment for a patient in a specific clinic
     */
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
           "AND a.clinicId = :clinicId " +
           "AND a.status = 'COMPLETED' " +
           "AND a.appointmentDate < :currentDate " +
           "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findLastCompletedAppointmentByPatientAndClinic(@Param("patientId") Long patientId,
                                                                     @Param("clinicId") Long clinicId,
                                                                     @Param("currentDate") LocalDate currentDate);

    /**
     * Find the next upcoming appointment for a patient in a specific clinic
     */
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
           "AND a.clinicId = :clinicId " +
           "AND a.status IN ('REQUESTED', 'CONFIRMED') " +
           "AND (a.appointmentDate > :currentDate OR " +
           "(a.appointmentDate = :currentDate AND a.startTime > :currentTime)) " +
           "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findNextUpcomingAppointmentByPatientAndClinic(@Param("patientId") Long patientId,
                                                                    @Param("clinicId") Long clinicId,
                                                                    @Param("currentDate") LocalDate currentDate,
                                                                    @Param("currentTime") LocalTime currentTime);

    /**
     * Find all patients who have appointments in a specific clinic
     */
    @Query("SELECT DISTINCT a.patientId FROM Appointment a WHERE a.clinicId = :clinicId")
    List<Long> findDistinctPatientIdsByClinicId(@Param("clinicId") Long clinicId);
}