package press.mizhifei.dentist.userprofile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.userprofile.model.Patient;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
} 