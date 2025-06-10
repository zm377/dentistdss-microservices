package press.mizhifei.dentist.appointment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.appointment.model.Appointment;
import press.mizhifei.dentist.appointment.model.AppointmentStatus;

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

    /**
     * Save appointment with proper casting for PostgreSQL enum types
     */
    @Query(nativeQuery = true, value = "INSERT INTO appointments " +
            "(id, patient_id, dentist_id, clinic_id, service_id, appointment_date, start_time, end_time, " +
            "status, reason_for_visit, symptoms, urgency, ai_triage_notes, notes, created_by, created_at, updated_at) " +
            "VALUES (nextval('appointment_id_seq'), :patientId, :dentistId, :clinicId, :serviceId, :appointmentDate, :startTime, :endTime, " +
            "CAST(:status AS appointment_status), :reasonForVisit, :symptoms, CAST(:urgency AS urgency_level), " +
            ":aiTriageNotes, :notes, :createdBy, NOW(), NOW()) RETURNING *")
    Appointment saveWithCasting(
            @Param("patientId") Long patientId,
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("serviceId") Integer serviceId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") String status,
            @Param("reasonForVisit") String reasonForVisit,
            @Param("symptoms") String symptoms,
            @Param("urgency") String urgency,
            @Param("aiTriageNotes") String aiTriageNotes,
            @Param("notes") String notes,
            @Param("createdBy") Long createdBy);

    /**
     * Update appointment status with proper casting for PostgreSQL enum types
     */
    @Query(nativeQuery = true, value = "UPDATE appointments SET " +
            "status = CAST(:status AS appointment_status), " +
            "confirmed_by = :confirmedBy, " +
            "updated_at = NOW() " +
            "WHERE id = :id RETURNING *")
    Appointment updateStatusWithCasting(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("confirmedBy") Long confirmedBy);

    /**
     * Update appointment with cancellation details using proper casting
     */
    @Query(nativeQuery = true, value = "UPDATE appointments SET " +
            "status = CAST(:status AS appointment_status), " +
            "cancellation_reason = :cancellationReason, " +
            "cancelled_by = :cancelledBy, " +
            "updated_at = NOW() " +
            "WHERE id = :id RETURNING *")
    Appointment updateCancellationWithCasting(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("cancellationReason") String cancellationReason,
            @Param("cancelledBy") Long cancelledBy);

    /**
     * Update appointment schedule with proper casting for PostgreSQL enum types
     */
    @Query(nativeQuery = true, value = "UPDATE appointments SET " +
            "appointment_date = :appointmentDate, " +
            "start_time = :startTime, " +
            "end_time = :endTime, " +
            "status = CAST(:status AS appointment_status), " +
            "updated_at = NOW() " +
            "WHERE id = :id RETURNING *")
    Appointment updateScheduleWithCasting(
            @Param("id") Long id,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("status") String status);

    /**
     * Update appointment status only with proper casting
     */
    @Query(nativeQuery = true, value = "UPDATE appointments SET " +
            "status = CAST(:status AS appointment_status), " +
            "updated_at = NOW() " +
            "WHERE id = :id RETURNING *")
    Appointment updateStatusOnlyWithCasting(
            @Param("id") Long id,
            @Param("status") String status);
}
