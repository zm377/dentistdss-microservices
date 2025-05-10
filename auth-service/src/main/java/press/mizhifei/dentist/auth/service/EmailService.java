package press.mizhifei.dentist.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author zhifeimi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email-verification.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String to, String firstName, String emailVerificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Complete Your Registration - DentistDSS");
            
            String verificationUrl = baseUrl + "/signup/verify?vtoken=" + emailVerificationToken;
            
            String htmlContent = String.format(
                "Hey %s!<br><br>" +
                "You are signing up to the dentist DSS. To complete the sign up, enter the verification code on the web page.<br><br>" +
                "Please click <a href=\"%s\">this link</a> to complete your sign up.<br><br>" +
                "Thanks,<br>" +
                "The DentistDSS Team", 
                firstName, verificationUrl);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Could not send verification email", e);
        }
    }
} 