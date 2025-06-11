package com.dentistdss.notification.service;

import com.dentistdss.notification.model.Notification;
import com.dentistdss.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PushNotificationService
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @InjectMocks
    private PushNotificationService pushNotificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.PUSH)
                .subject("Test Notification")
                .body("This is a test notification")
                .build();
    }

    @Test
    void registerDeviceToken_ValidToken_Success() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";

        // When
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // Then
        assertTrue(pushNotificationService.hasDeviceToken(userId));
    }

    @Test
    void registerDeviceToken_NullToken_DoesNotRegister() {
        // Given
        Long userId = 1L;
        String deviceToken = null;

        // When
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // Then
        assertFalse(pushNotificationService.hasDeviceToken(userId));
    }

    @Test
    void registerDeviceToken_EmptyToken_DoesNotRegister() {
        // Given
        Long userId = 1L;
        String deviceToken = "";

        // When
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // Then
        assertFalse(pushNotificationService.hasDeviceToken(userId));
    }

    @Test
    void unregisterDeviceToken_ExistingToken_Success() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // When
        pushNotificationService.unregisterDeviceToken(userId);

        // Then
        assertFalse(pushNotificationService.hasDeviceToken(userId));
    }

    @Test
    void sendPushNotification_WithRegisteredToken_Success() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            pushNotificationService.sendPushNotification(userId, "Test Title", "Test Body");
        });
    }

    @Test
    void sendPushNotification_WithoutRegisteredToken_LogsWarning() {
        // Given
        Long userId = 1L;
        // No device token registered

        // When & Then (should not throw exception, just log warning)
        assertDoesNotThrow(() -> {
            pushNotificationService.sendPushNotification(userId, "Test Title", "Test Body");
        });
    }

    @Test
    void sendPushNotification_WithData_Success() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";
        pushNotificationService.registerDeviceToken(userId, deviceToken);
        
        Map<String, String> data = new HashMap<>();
        data.put("appointment_id", "123");
        data.put("clinic_id", "456");

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            pushNotificationService.sendPushNotification(userId, "Test Title", "Test Body", data);
        });
    }

    @Test
    void sendPushNotification_FromNotificationEntity_Success() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> {
            pushNotificationService.sendPushNotification(notification);
        });
    }

    @Test
    void hasDeviceToken_WithRegisteredToken_ReturnsTrue() {
        // Given
        Long userId = 1L;
        String deviceToken = "test-device-token-123";
        pushNotificationService.registerDeviceToken(userId, deviceToken);

        // When
        boolean hasToken = pushNotificationService.hasDeviceToken(userId);

        // Then
        assertTrue(hasToken);
    }

    @Test
    void hasDeviceToken_WithoutRegisteredToken_ReturnsFalse() {
        // Given
        Long userId = 1L;
        // No device token registered

        // When
        boolean hasToken = pushNotificationService.hasDeviceToken(userId);

        // Then
        assertFalse(hasToken);
    }
}
