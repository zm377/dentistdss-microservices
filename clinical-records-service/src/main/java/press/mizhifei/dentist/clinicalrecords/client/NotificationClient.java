package press.mizhifei.dentist.clinicalrecords.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for notification-service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "notification-service", path = "/notification")
public interface NotificationClient {
    
    @PostMapping("/send")
    void sendNotification(@RequestBody Map<String, Object> notificationRequest);
}
