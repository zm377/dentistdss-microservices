package press.mizhifei.dentist.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.model.AuthProvider;

import java.util.Optional;

/**
 * @author zhifeimi
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByProviderIdAndProvider(String providerId, AuthProvider provider);
}
