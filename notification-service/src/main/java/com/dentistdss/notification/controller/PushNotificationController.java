package com.dentistdss.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.notification.dto.ApiResponse;
import com.dentistdss.notification.service.PushNotificationService;

/**
 * Controller for push notification management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/notification/push")
@RequiredArgsConstructor
public class PushNotificationController {
    
    private final PushNotificationService pushNotificationService;
    
    /**
     * Register device token for push notifications
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerDeviceToken(
            @Valid @RequestBody DeviceTokenRequest request) {
        
        try {
            pushNotificationService.registerDeviceToken(request.getUserId(), request.getDeviceToken());
            return ResponseEntity.ok(ApiResponse.success("Device token registered successfully"));
        } catch (Exception e) {
            log.error("Failed to register device token for user {}: {}", request.getUserId(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to register device token"));
        }
    }
    
    /**
     * Unregister device token
     */
    @DeleteMapping("/unregister/{userId}")
    public ResponseEntity<ApiResponse<String>> unregisterDeviceToken(@PathVariable Long userId) {
        try {
            pushNotificationService.unregisterDeviceToken(userId);
            return ResponseEntity.ok(ApiResponse.success("Device token unregistered successfully"));
        } catch (Exception e) {
            log.error("Failed to unregister device token for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to unregister device token"));
        }
    }
    
    /**
     * Check if user has registered device token
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> getRegistrationStatus(@PathVariable Long userId) {
        boolean hasToken = pushNotificationService.hasDeviceToken(userId);
        return ResponseEntity.ok(ApiResponse.success(hasToken));
    }
    
    /**
     * Send test push notification
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> sendTestNotification(
            @Valid @RequestBody TestPushRequest request) {
        
        try {
            pushNotificationService.sendPushNotification(
                request.getUserId(),
                request.getTitle(),
                request.getBody()
            );
            return ResponseEntity.ok(ApiResponse.success("Test push notification sent"));
        } catch (Exception e) {
            log.error("Failed to send test push notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send test notification"));
        }
    }
    
    /**
     * Device token registration request
     */
    public static class DeviceTokenRequest {
        private Long userId;
        private String deviceToken;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    }
    
    /**
     * Test push notification request
     */
    public static class TestPushRequest {
        private Long userId;
        private String title;
        private String body;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
}
