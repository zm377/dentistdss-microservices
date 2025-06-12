package com.dentistdss.auth.controller;

import com.dentistdss.auth.dto.ApiResponse;
import com.dentistdss.auth.model.User;
import com.dentistdss.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Debug controller for user account issues
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@RestController
@RequestMapping("/auth/debug")
@RequiredArgsConstructor
public class UserDebugController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/user/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatus(@PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        
        User user = userOptional.get();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("enabled", user.isEnabled());
        userInfo.put("emailVerified", user.isEmailVerified());
        userInfo.put("accountNonExpired", user.isAccountNonExpired());
        userInfo.put("accountNonLocked", user.isAccountNonLocked());
        userInfo.put("credentialsNonExpired", user.isCredentialsNonExpired());
        userInfo.put("roles", user.getRoles());
        userInfo.put("provider", user.getProvider());
        userInfo.put("clinicId", user.getClinicId());
        userInfo.put("approvalStatus", user.getApprovalStatus());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PostMapping("/user/{email}/enable")
    public ResponseEntity<ApiResponse<String>> enableUser(@PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        
        User user = userOptional.get();
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User {} has been enabled and email verified", email);
        return ResponseEntity.ok(ApiResponse.successMessage("User enabled successfully"));
    }

    @PostMapping("/user/{email}/verify-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPassword(
            @PathVariable String email, 
            @RequestParam String password) {
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        
        User user = userOptional.get();
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("passwordMatches", passwordMatches);
        result.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
        result.put("passwordLength", user.getPassword() != null ? user.getPassword().length() : 0);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/user/{email}/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable String email, 
            @RequestParam String newPassword) {
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.ok(ApiResponse.error("User not found"));
        }
        
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password reset for user: {}", email);
        return ResponseEntity.ok(ApiResponse.successMessage("Password reset successfully"));
    }
}
