package com.dentistdss.clinicalrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicalrecords.model.TreatmentPlan;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer> {
    
    List<TreatmentPlan> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    List<TreatmentPlan> findByDentistIdOrderByCreatedAtDesc(Long dentistId);
    
    List<TreatmentPlan> findByPatientIdAndStatus(Long patientId, String status);
    
    List<TreatmentPlan> findByClinicIdAndStatus(Long clinicId, String status);
    
    @Query("SELECT tp FROM TreatmentPlan tp WHERE tp.parentPlanId = :parentPlanId " +
           "ORDER BY tp.version DESC")
    List<TreatmentPlan> findPlanVersions(@Param("parentPlanId") Integer parentPlanId);
    
    @Query("SELECT tp FROM TreatmentPlan tp WHERE tp.patientId = :patientId " +
           "AND tp.parentPlanId IS NULL ORDER BY tp.createdAt DESC")
    List<TreatmentPlan> findOriginalPlansByPatient(@Param("patientId") Long patientId);
}
