package press.mizhifei.dentist.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import press.mizhifei.dentist.notification.client.AuthServiceClient;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final AuthServiceClient authServiceClient;
    
    public void sendEmail(Long userId, String subject, String body) {
        try {
            String userEmail = authServiceClient.getUserEmail(userId);
            sendEmail(userEmail, subject, body, false);
        } catch (Exception e) {
            log.error("Failed to fetch email for user {}: {}", userId, e.getMessage());
            // Fallback for development/testing
            String fallbackEmail = "user" + userId + "@example.com";
            log.warn("Using fallback email {} for user {}", fallbackEmail, userId);
            sendEmail(fallbackEmail, subject, body, false);
        }
    }
    
    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        try {
            if (isHtml) {
                sendHtmlEmail(to, subject, body);
            } else {
                sendSimpleEmail(to, subject, body);
            }
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@dentistdss.com");
        
        mailSender.send(message);
    }
    
    private void sendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.setFrom("noreply@dentistdss.com");
        
        mailSender.send(message);
    }
} 