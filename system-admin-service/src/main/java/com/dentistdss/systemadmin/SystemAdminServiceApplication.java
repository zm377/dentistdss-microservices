package com.dentistdss.systemadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * System Administration Service Application
 * 
 * Comprehensive system administration microservice providing:
 * - Rate limiting configuration management
 * - System parameter configuration
 * - Configuration refresh orchestration across all microservices
 * - System-wide audit and monitoring capabilities
 * - SYSTEM_ADMIN role-based access control
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
public class SystemAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemAdminServiceApplication.class, args);
    }
}
