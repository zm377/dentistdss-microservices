package com.dentistdss.auth.service;

import com.dentistdss.auth.dto.LoginRequest;
import com.dentistdss.auth.dto.ApiResponse;
import com.dentistdss.auth.model.User;
import com.dentistdss.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test for login issues
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootTest
@ActiveProfiles("test")
class LoginDebugTest {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void debugUserLogin() {
        String email = "phenix.zhifei.mi@gmail.com";
        String password = "gwF6MC.PT#FLyb7";
        
        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        assertTrue(userOptional.isPresent(), "User should exist in database");
        
        User user = userOptional.get();
        System.out.println("=== USER DEBUG INFO ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Email: " + user.getEmail());
        System.out.println("First Name: " + user.getFirstName());
        System.out.println("Last Name: " + user.getLastName());
        System.out.println("Enabled: " + user.isEnabled());
        System.out.println("Email Verified: " + user.isEmailVerified());
        System.out.println("Account Non Expired: " + user.isAccountNonExpired());
        System.out.println("Account Non Locked: " + user.isAccountNonLocked());
        System.out.println("Credentials Non Expired: " + user.isCredentialsNonExpired());
        System.out.println("Roles: " + user.getRoles());
        System.out.println("Provider: " + user.getProvider());
        System.out.println("Clinic ID: " + user.getClinicId());
        System.out.println("Approval Status: " + user.getApprovalStatus());
        
        // Check password
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Password matches: " + passwordMatches);
        
        // Try login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        ApiResponse<?> response = authService.authenticateUser(loginRequest);
        System.out.println("Login response success: " + response.isSuccess());
        System.out.println("Login response message: " + response.getMessage());
        
        if (!response.isSuccess()) {
            System.out.println("=== LOGIN FAILURE ANALYSIS ===");
            if (!user.isEnabled()) {
                if (!user.isEmailVerified()) {
                    System.out.println("ISSUE: Email not verified");
                } else {
                    System.out.println("ISSUE: Account not enabled despite email verification");
                }
            } else if (!passwordMatches) {
                System.out.println("ISSUE: Password does not match");
            } else {
                System.out.println("ISSUE: Unknown authentication failure");
            }
        }
    }
}
