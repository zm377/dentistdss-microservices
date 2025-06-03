package press.mizhifei.dentist.clinic.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import press.mizhifei.dentist.clinic.dto.ApiResponse;

import java.util.Map;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "notification-service", path = "/notification")
public interface NotificationClient {
    
    @PostMapping("/send")
    ApiResponse<Object> sendNotification(@RequestBody Map<String, Object> request);
} 