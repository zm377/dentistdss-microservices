package press.mizhifei.dentist.oauth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import press.mizhifei.dentist.oauth.model.User;

import java.util.Optional;

/**
 * @author zhifeimi
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
} 