package press.mizhifei.dentist.clinicalrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinicalrecords.model.TreatmentPlanItem;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface TreatmentPlanItemRepository extends JpaRepository<TreatmentPlanItem, Integer> {
    
    List<TreatmentPlanItem> findByTreatmentPlanIdOrderBySequenceOrder(Integer treatmentPlanId);
    
    List<TreatmentPlanItem> findByTreatmentPlanIdAndStatus(Integer treatmentPlanId, String status);
}
