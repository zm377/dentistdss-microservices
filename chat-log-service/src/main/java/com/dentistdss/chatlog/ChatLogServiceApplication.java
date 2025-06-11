package com.dentistdss.chatlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Chat Log Service Application
 * 
 * Comprehensive chat logging service for the DentistDSS system that:
 * - Persists all conversational interactions between users and AI
 * - Supports anonymous users with session-based tracking
 * - Implements PHI redaction for HIPAA compliance
 * - Provides token tracking and usage analytics
 * - Enables dentists to review patient conversation history
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ChatLogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatLogServiceApplication.class, args);
    }
}
