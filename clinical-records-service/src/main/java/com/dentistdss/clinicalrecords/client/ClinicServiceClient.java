package com.dentistdss.clinicalrecords.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for clinic-service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "clinic-service", path = "/clinic")
public interface ClinicServiceClient {
    
    @GetMapping("/{id}")
    ClinicResponse getClinic(@PathVariable("id") Long clinicId);
    
    @GetMapping("/service/{id}")
    ServiceResponse getService(@PathVariable("id") Integer serviceId);
}
