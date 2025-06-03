package press.mizhifei.dentist.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinic.model.TreatmentPlan;

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
} 