package com.dentistdss.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Workflow Service Application
 * 
 * Central workflow orchestration service for DentistDSS system.
 * Manages business workflow processes including user approval workflows,
 * appointment workflows, and other configurable business processes.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootApplication
@EnableFeignClients
public class WorkflowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowServiceApplication.class, args);
    }
}
