package press.mizhifei.dentist.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.AuthResponse;
import press.mizhifei.dentist.auth.dto.OAuthLoginRequest;
import press.mizhifei.dentist.auth.model.AuthProvider;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.repository.UserRepository;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;
import press.mizhifei.dentist.auth.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

/**
 * Service for handling OAuth user processing without circular dependencies.
 * This service is responsible for OAuth user creation, linking, and token generation.
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public ApiResponse<AuthResponse> processOAuthLogin(OAuthLoginRequest oAuthLoginRequest) {
        log.debug("Processing OAuth login for email: {}", oAuthLoginRequest.getEmail());
        
        Optional<User> userOptionalByProviderId = userRepository.findByProviderIdAndProvider(
                oAuthLoginRequest.getProviderId(), 
                AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()));

        User user;
        if (userOptionalByProviderId.isPresent()) {
            // User found by provider ID, this is a returning OAuth user
            user = userOptionalByProviderId.get();
            log.debug("Found existing OAuth user: {}", user.getEmail());
            
            // Check if user is enabled for non-patient roles
            if ((user.getRoles().contains(Role.CLINIC_ADMIN)
                    || user.getRoles().contains(Role.DENTIST)
                    || user.getRoles().contains(Role.RECEPTIONIST))
                    && !user.isEnabled()) {
                log.warn("OAuth login attempt for disabled non-patient user: {}", user.getEmail());
                return ApiResponse.error("Your account is not activated, please contact the administrator");
            }
            
            // Update user info if needed
            user.setFirstName(oAuthLoginRequest.getFirstName());
            user.setLastName(oAuthLoginRequest.getLastName());
            user.setLastLoginAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
            
        } else {
            // No user found by provider ID, check by email
            Optional<User> userOptionalByEmail = userRepository.findByEmail(oAuthLoginRequest.getEmail());
            if (userOptionalByEmail.isPresent()) {
                // User found by email, link this OAuth account to the existing local account
                user = userOptionalByEmail.get();
                log.debug("Linking OAuth provider to existing user: {}", user.getEmail());
                
                user.setProvider(AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()));
                user.setProviderId(oAuthLoginRequest.getProviderId());
                
                // Update names if they're empty in local but available from OAuth
                if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                    user.setFirstName(oAuthLoginRequest.getFirstName());
                }
                if (user.getLastName() == null || user.getLastName().isEmpty()) {
                    user.setLastName(oAuthLoginRequest.getLastName());
                }
                
                user.setEmailVerified(true); // Email is verified by OAuth provider
                user.setEnabled(true); // Enable account for OAuth users
                user.setLastLoginAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user = userRepository.save(user);
                
            } else {
                // New user, create an account
                log.debug("Creating new OAuth user: {}", oAuthLoginRequest.getEmail());
                
                user = User.builder()
                        .email(oAuthLoginRequest.getEmail())
                        .firstName(oAuthLoginRequest.getFirstName())
                        .lastName(oAuthLoginRequest.getLastName())
                        .provider(AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()))
                        .providerId(oAuthLoginRequest.getProviderId())
                        .roles(new HashSet<>(Collections.singleton(Role.PATIENT))) // Default role for new OAuth users
                        .emailVerified(true) // Email is verified by OAuth provider
                        .enabled(true)
                        .accountNonExpired(true)
                        .credentialsNonExpired(true)
                        .accountNonLocked(true)
                        .lastLoginAt(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);
            }
        }

        // Generate JWT token for the user without using AuthenticationManager
        AuthResponse authResponse = generateTokenForUser(user);
        log.info("OAuth login successful for user: {}", user.getEmail());
        return ApiResponse.success(authResponse);
    }

    /**
     * Generate JWT token for OAuth user without using AuthenticationManager
     * to avoid circular dependency issues.
     */
    private AuthResponse generateTokenForUser(User user) {
        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Create authentication token with UserPrincipal as principal
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null, // No credentials needed for OAuth
                principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(user.toUserResponse())
                .build();
    }
}
