package com.dentistdss.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dentistdss.notification.model.Notification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Push Notification Service for handling push notifications
 * This is a basic implementation that can be extended with FCM, APNs, or other push services
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {
    
    // In-memory storage for device tokens (in production, this should be in database)
    private final Map<Long, String> userDeviceTokens = new ConcurrentHashMap<>();
    
    /**
     * Send push notification to user
     */
    public void sendPushNotification(Long userId, String title, String body) {
        String deviceToken = userDeviceTokens.get(userId);
        
        if (deviceToken == null) {
            log.warn("No device token found for user {}", userId);
            return;
        }
        
        try {
            // In a real implementation, this would integrate with FCM, APNs, etc.
            log.info("Sending push notification to user {} (token: {}): {} - {}", 
                    userId, deviceToken, title, body);
            
            // Simulate push notification sending
            simulatePushNotification(deviceToken, title, body);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Push notification failed", e);
        }
    }
    
    /**
     * Send push notification with custom data
     */
    public void sendPushNotification(Long userId, String title, String body, Map<String, String> data) {
        String deviceToken = userDeviceTokens.get(userId);
        
        if (deviceToken == null) {
            log.warn("No device token found for user {}", userId);
            return;
        }
        
        try {
            log.info("Sending push notification with data to user {} (token: {}): {} - {}", 
                    userId, deviceToken, title, body);
            
            // Simulate push notification with data
            simulatePushNotificationWithData(deviceToken, title, body, data);
            
        } catch (Exception e) {
            log.error("Failed to send push notification with data to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Push notification failed", e);
        }
    }
    
    /**
     * Register device token for user
     */
    public void registerDeviceToken(Long userId, String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            log.warn("Invalid device token provided for user {}", userId);
            return;
        }
        
        userDeviceTokens.put(userId, deviceToken);
        log.info("Registered device token for user {}", userId);
    }
    
    /**
     * Unregister device token for user
     */
    public void unregisterDeviceToken(Long userId) {
        String removedToken = userDeviceTokens.remove(userId);
        if (removedToken != null) {
            log.info("Unregistered device token for user {}", userId);
        }
    }
    
    /**
     * Check if user has registered device token
     */
    public boolean hasDeviceToken(Long userId) {
        return userDeviceTokens.containsKey(userId);
    }
    
    /**
     * Send push notification from notification entity
     */
    public void sendPushNotification(Notification notification) {
        sendPushNotification(
            notification.getUserId(),
            notification.getSubject(),
            notification.getBody()
        );
    }
    
    /**
     * Simulate push notification sending (replace with actual FCM/APNs implementation)
     */
    private void simulatePushNotification(String deviceToken, String title, String body) {
        // In production, this would be replaced with actual push service calls
        log.info("PUSH NOTIFICATION SENT:");
        log.info("  Device Token: {}", deviceToken);
        log.info("  Title: {}", title);
        log.info("  Body: {}", body);
        log.info("  Status: SUCCESS");
    }
    
    /**
     * Simulate push notification with data sending
     */
    private void simulatePushNotificationWithData(String deviceToken, String title, String body, Map<String, String> data) {
        log.info("PUSH NOTIFICATION WITH DATA SENT:");
        log.info("  Device Token: {}", deviceToken);
        log.info("  Title: {}", title);
        log.info("  Body: {}", body);
        log.info("  Data: {}", data);
        log.info("  Status: SUCCESS");
    }
}
