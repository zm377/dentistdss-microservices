package com.dentistdss.userprofile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.dentistdss.userprofile.dto.NotificationEmailRequest;

/**
 * Feign client for notification service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "notification-service", path = "/notification/email")
public interface NotificationServiceClient {

    @PostMapping("/notification")
    ResponseEntity<String> sendNotificationEmail(@RequestBody NotificationEmailRequest request);
}
