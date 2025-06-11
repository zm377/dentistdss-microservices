package com.dentistdss.notification.service;

import com.dentistdss.notification.dto.NotificationRequest;
import com.dentistdss.notification.dto.NotificationResponse;
import com.dentistdss.notification.model.Notification;
import com.dentistdss.notification.model.NotificationStatus;
import com.dentistdss.notification.model.NotificationTemplate;
import com.dentistdss.notification.model.NotificationType;
import com.dentistdss.notification.repository.NotificationRepository;
import com.dentistdss.notification.repository.NotificationTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest notificationRequest;
    private Notification notification;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("patient_name", "John Doe");
        templateVariables.put("appointment_date", "2024-01-15");

        notificationRequest = NotificationRequest.builder()
                .userId(1L)
                .templateName("appointment_confirmation")
                .type("EMAIL")
                .templateVariables(templateVariables)
                .build();

        template = NotificationTemplate.builder()
                .id(1L)
                .name("appointment_confirmation")
                .subject("Appointment Confirmation")
                .body("Dear {{patient_name}}, your appointment is confirmed for {{appointment_date}}")
                .type(NotificationType.EMAIL)
                .isActive(true)
                .build();

        notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.EMAIL)
                .subject("Appointment Confirmation")
                .body("Dear John Doe, your appointment is confirmed for 2024-01-15")
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendNotification_EmailType_Success() {
        // Given
        when(templateRepository.findByNameAndIsActiveTrue("appointment_confirmation"))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        NotificationResponse response = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(response);
        assertEquals(notification.getId(), response.getId());
        assertEquals(NotificationStatus.SENT, response.getStatus());
        verify(emailService).sendEmail(anyString(), anyString(), anyString());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_PushType_Success() {
        // Given
        notificationRequest.setType("PUSH");
        template.setType(NotificationType.PUSH);
        notification.setType(NotificationType.PUSH);
        
        when(templateRepository.findByNameAndIsActiveTrue("appointment_confirmation"))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(pushNotificationService).sendPushNotification(any(Notification.class));

        // When
        NotificationResponse response = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(response);
        assertEquals(notification.getId(), response.getId());
        verify(pushNotificationService).sendPushNotification(any(Notification.class));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_TemplateNotFound_ThrowsException() {
        // Given
        when(templateRepository.findByNameAndIsActiveTrue("appointment_confirmation"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(notificationRequest);
        });
    }

    @Test
    void sendNotification_EmailServiceFails_SetsFailedStatus() {
        // Given
        when(templateRepository.findByNameAndIsActiveTrue("appointment_confirmation"))
                .thenReturn(Optional.of(template));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        // When
        NotificationResponse response = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(response);
        assertEquals(NotificationStatus.FAILED, response.getStatus());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void processTemplate_WithVariables_Success() {
        // Given
        String templateBody = "Dear {{patient_name}}, your appointment is on {{appointment_date}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("patient_name", "John Doe");
        variables.put("appointment_date", "2024-01-15");

        // When
        String result = notificationService.processTemplate(templateBody, variables);

        // Then
        assertEquals("Dear John Doe, your appointment is on 2024-01-15", result);
    }

    @Test
    void processTemplate_NoVariables_ReturnsOriginal() {
        // Given
        String templateBody = "Simple message without variables";

        // When
        String result = notificationService.processTemplate(templateBody, null);

        // Then
        assertEquals("Simple message without variables", result);
    }

    @Test
    void processTemplate_MissingVariable_KeepsPlaceholder() {
        // Given
        String templateBody = "Dear {{patient_name}}, your appointment is on {{appointment_date}}";
        Map<String, String> variables = new HashMap<>();
        variables.put("patient_name", "John Doe");
        // Missing appointment_date

        // When
        String result = notificationService.processTemplate(templateBody, variables);

        // Then
        assertEquals("Dear John Doe, your appointment is on {{appointment_date}}", result);
    }
}
