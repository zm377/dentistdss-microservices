package com.dentistdss.workflow.client;

import com.dentistdss.workflow.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for notification-service
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@FeignClient(name = "notification-service", path = "/notification")
public interface NotificationServiceClient {
    
    @PostMapping("/email/template")
    ResponseEntity<ApiResponse<String>> sendNotificationEmail(@RequestBody Map<String, Object> request);
    
    @PostMapping("/sms")
    ResponseEntity<ApiResponse<String>> sendSmsNotification(@RequestBody Map<String, Object> request);
    
    @PostMapping("/push")
    ResponseEntity<ApiResponse<String>> sendPushNotification(@RequestBody Map<String, Object> request);
}
