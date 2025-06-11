package com.dentistdss.clinicadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Clinic Administration Service Application
 * 
 * Focused microservice for clinic administration capabilities including:
 * - Clinic CRUD operations
 * - Working hours management
 * - Holiday management
 * - Role-based access control for clinic administrators
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClinicAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicAdminServiceApplication.class, args);
    }
}
