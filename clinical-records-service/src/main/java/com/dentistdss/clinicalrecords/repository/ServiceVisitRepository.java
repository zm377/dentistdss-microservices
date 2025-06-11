package com.dentistdss.clinicalrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicalrecords.model.ServiceVisit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface ServiceVisitRepository extends JpaRepository<ServiceVisit, Long> {
    
    List<ServiceVisit> findByPatientIdOrderByVisitDateDesc(Long patientId);
    
    List<ServiceVisit> findByDentistIdOrderByVisitDateDesc(Long dentistId);
    
    List<ServiceVisit> findByClinicIdOrderByVisitDateDesc(Long clinicId);
    
    Optional<ServiceVisit> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT sv FROM ServiceVisit sv WHERE sv.patientId = :patientId " +
           "AND sv.visitDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sv.visitDate DESC")
    List<ServiceVisit> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sv FROM ServiceVisit sv WHERE sv.clinicId = :clinicId " +
           "AND sv.status = :status ORDER BY sv.visitDate DESC")
    List<ServiceVisit> findByClinicIdAndStatus(@Param("clinicId") Long clinicId,
                                              @Param("status") String status);
}
