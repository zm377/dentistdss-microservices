package com.dentistdss.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.dentistdss.auth.dto.EmailRequest;
import com.dentistdss.auth.dto.NotificationEmailRequest;
import com.dentistdss.auth.dto.ProcessingReminderEmailRequest;
import com.dentistdss.auth.dto.SystemAdminApprovalEmailRequest;
import com.dentistdss.auth.dto.VerificationEmailRequest;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "notification-service", path = "/notification/email")
public interface NotificationServiceClient {

    @PostMapping("/send")
    ResponseEntity<String> sendEmail(@RequestBody EmailRequest request);

    @PostMapping("/verification")
    ResponseEntity<String> sendVerificationEmail(@RequestBody VerificationEmailRequest request);

    @PostMapping("/processing-reminder")
    ResponseEntity<String> sendProcessingReminderEmail(@RequestBody ProcessingReminderEmailRequest request);

    @PostMapping("/system-admin-approval")
    ResponseEntity<String> sendSystemAdminApprovalEmail(@RequestBody SystemAdminApprovalEmailRequest request);

    @PostMapping("/notification")
    ResponseEntity<String> sendNotificationEmail(@RequestBody NotificationEmailRequest request);
}
