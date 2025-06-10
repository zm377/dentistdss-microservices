package press.mizhifei.dentist.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.notification.dto.EmailRequest;
import press.mizhifei.dentist.notification.dto.NotificationEmailRequest;
import press.mizhifei.dentist.notification.dto.ProcessingReminderEmailRequest;
import press.mizhifei.dentist.notification.dto.SystemAdminApprovalEmailRequest;
import press.mizhifei.dentist.notification.dto.VerificationEmailRequest;
import press.mizhifei.dentist.notification.service.EmailService;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@RestController
@RequestMapping("/notification/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody(), request.isHtml());
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/verification")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody VerificationEmailRequest request) {
        try {
            if (request.getType().equals("token")) {
                emailService.sendVerificationEmail(request.getTo(), request.getVerificationValue());
            } else {
                emailService.sendVerificationCode(request.getTo(), request.getVerificationValue());
            }
            return ResponseEntity.ok("Verification email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send verification email: " + e.getMessage());
        }
    }

    @PostMapping("/processing-reminder")
    public ResponseEntity<String> sendProcessingReminderEmail(@RequestBody ProcessingReminderEmailRequest request) {
        try {
            emailService.sendProcessingReminderEmail(
                request.getClinicAdminEmail(),
                request.getClinicName(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole()
            );
            return ResponseEntity.ok("Processing reminder email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send processing reminder email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send processing reminder email: " + e.getMessage());
        }
    }

    @PostMapping("/system-admin-approval")
    public ResponseEntity<String> sendSystemAdminApprovalEmail(@RequestBody SystemAdminApprovalEmailRequest request) {
        try {
            emailService.sendSystemAdminApprovalEmail(
                request.getSystemAdminEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getClinicName(),
                request.getAddress(),
                request.getCity(),
                request.getState(),
                request.getZipCode(),
                request.getCountry(),
                request.getPhoneNumber(),
                request.getBusinessEmail()
            );
            return ResponseEntity.ok("System admin approval email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send system admin approval email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send system admin approval email: " + e.getMessage());
        }
    }

    @PostMapping("/notification")
    public ResponseEntity<String> sendNotificationEmail(@RequestBody NotificationEmailRequest request) {
        try {
            emailService.sendNotificationEmail(request.getTo(), request.getTemplateName(), request.getVariables());
            return ResponseEntity.ok("Notification email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send notification email: " + e.getMessage());
        }
    }
}
