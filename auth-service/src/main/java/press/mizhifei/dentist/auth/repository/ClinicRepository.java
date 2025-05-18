package press.mizhifei.dentist.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import press.mizhifei.dentist.auth.model.Clinic;

/**
 * @author zhifeimi
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    @NonNull
    Optional<Clinic> findById(@NonNull Long id);
}
