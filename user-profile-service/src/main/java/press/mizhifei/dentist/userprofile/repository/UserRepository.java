package press.mizhifei.dentist.userprofile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.userprofile.model.Role;
import press.mizhifei.dentist.userprofile.model.User;

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
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByVerificationCode(String code);
    
    @Query("SELECT u FROM User u WHERE u.clinicId = :clinicId")
    List<User> findByClinicId(@Param("clinicId") Long clinicId);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.clinicId = :clinicId")
    List<User> findByRoleAndClinicId(@Param("role") Role role, @Param("clinicId") Long clinicId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.clinicId = :clinicId AND r = :role")
    List<User> findByClinicIdAndRoles(@Param("clinicId") Long clinicId, @Param("role") Role role);
}
