package com.dentistdss.notification.client;

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
    
    @GetMapping("/user/{id}/email")
    String getUserEmail(@PathVariable("id") Long userId);
    
    @GetMapping("/user/{id}/name")
    String getUserFullName(@PathVariable("id") Long userId);
} 