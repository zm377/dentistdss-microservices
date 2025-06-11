package com.dentistdss.clinicalrecords;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClinicalRecordsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicalRecordsServiceApplication.class, args);
    }
}
