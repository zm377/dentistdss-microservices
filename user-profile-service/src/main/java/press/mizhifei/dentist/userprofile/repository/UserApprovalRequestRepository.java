package press.mizhifei.dentist.userprofile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.userprofile.model.UserApprovalRequest;

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
public interface UserApprovalRequestRepository extends JpaRepository<UserApprovalRequest, Integer> {
    
    Optional<UserApprovalRequest> findByUserIdAndStatus(Long userId, String status);
    
    List<UserApprovalRequest> findByStatus(String status);
    
    List<UserApprovalRequest> findByClinicIdAndStatusOrderByCreatedAtDesc(Long clinicId, String status);
    
    List<UserApprovalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<UserApprovalRequest> findByClinicIdOrderByCreatedAtDesc(Long clinicId);
}
