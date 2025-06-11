package com.dentistdss.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    
    private final NotificationService notificationService;
    
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void processScheduledNotifications() {
        log.debug("Processing scheduled notifications...");
        try {
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("Error processing scheduled notifications: {}", e.getMessage());
        }
    }
} 