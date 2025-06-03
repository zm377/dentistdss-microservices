package press.mizhifei.dentist.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinic.model.Clinic;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    List<Clinic> findByEnabledTrue();

    @Query("SELECT c FROM Clinic c WHERE c.enabled = true " +
            "AND (:keywords IS NULL OR :keywords = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.state) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.zipCode) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.country) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keywords, '%')))")
    List<Clinic> searchClinics(@Param("keywords") String keywords);
} 