package press.mizhifei.dentist.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.notification.model.NotificationTemplate;
import press.mizhifei.dentist.notification.model.NotificationType;

import java.util.Optional;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {
    
    Optional<NotificationTemplate> findByNameAndIsActiveTrue(String name);
    
    Optional<NotificationTemplate> findByNameAndTypeAndIsActiveTrue(String name, NotificationType type);
} 