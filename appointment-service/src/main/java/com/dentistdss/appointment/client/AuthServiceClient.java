package com.dentistdss.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {
    
    @GetMapping("/user/{userId}/full-name")
    String getUserFullName(@PathVariable("userId") Long userId);
}
