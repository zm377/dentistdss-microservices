package com.dentistdss.workflow.config;

import com.dentistdss.workflow.service.UserApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for workflow service
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Configuration
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class WorkflowConfiguration {
    
    private final UserApprovalWorkflowService userApprovalWorkflowService;
    
    /**
     * Initialize workflow definitions on startup
     */
    @Bean
    public CommandLineRunner initializeWorkflowDefinitions() {
        return args -> {
            log.info("Initializing workflow definitions...");
            try {
                userApprovalWorkflowService.ensureWorkflowDefinitionsExist();
                log.info("Workflow definitions initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize workflow definitions: {}", e.getMessage(), e);
            }
        };
    }
}
