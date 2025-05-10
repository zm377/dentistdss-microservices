package press.mizhifei.dentist.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.AuthResponse;
import press.mizhifei.dentist.auth.dto.LoginRequest;
import press.mizhifei.dentist.auth.dto.SignUpRequest;
import press.mizhifei.dentist.auth.model.AuthProvider;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.repository.UserRepository;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;
import press.mizhifei.dentist.auth.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * @author zhifeimi
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    
    @Value("${app.email-verification.token-expiry-minutes}")
    private long tokenExpiryMinutes;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .firstName(userPrincipal.getFirstName())
                .lastName(userPrincipal.getLastName())
                .roles(user.getRoles())
                .build();
    }

    @Transactional
    public ApiResponse registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        
        // Generate verification token
        String emailVerificationToken = generateVerificationToken();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);
        
        User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(Collections.singleton(Role.PATIENT))
                .emailVerified(false)
                .emailVerificationToken(emailVerificationToken)
                .emailVerificationTokenExpiry(tokenExpiry)
                .enabled(false) // User is not enabled until email is verified
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(
                savedUser.getEmail(), 
                savedUser.getFirstName(),
                emailVerificationToken
        );

        return new ApiResponse(true, "User registered successfully. Please check your email to complete registration.");
    }
    
    @Transactional
    public AuthResponse verifyEmailAndLogin(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (user.isEmailVerified()) {
            // User already verified, just log them in
            return authenticateAndGenerateToken(user);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (user.getEmailVerificationTokenExpiry().isBefore(now)) {
            throw new RuntimeException("Verification token has expired");
        }
        
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        
        userRepository.save(user);
        
        return authenticateAndGenerateToken(user);
    }
    
    private AuthResponse authenticateAndGenerateToken(User user) {
        // Create authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null, // No credentials needed as we're authenticating directly
                UserPrincipal.create(user).getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }
    
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}
