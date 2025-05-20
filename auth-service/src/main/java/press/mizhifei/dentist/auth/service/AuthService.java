package press.mizhifei.dentist.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.val;

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
import press.mizhifei.dentist.auth.dto.ChangePasswordRequest;
import press.mizhifei.dentist.auth.dto.LoginRequest;
import press.mizhifei.dentist.auth.dto.OAuthLoginRequest;
import press.mizhifei.dentist.auth.dto.SignUpRequest;
import press.mizhifei.dentist.auth.dto.SignUpStaffRequest;
import press.mizhifei.dentist.auth.dto.UserResponse;
import press.mizhifei.dentist.auth.model.AuthProvider;
import press.mizhifei.dentist.auth.model.Clinic;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.repository.ClinicRepository;
import press.mizhifei.dentist.auth.repository.UserRepository;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;
import press.mizhifei.dentist.auth.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
/**
 * @author zhifeimi
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    
    @Value("${app.email-verification.token-expiry-minutes}")
    private long tokenExpiryMinutes;

    @Value("${app.email-verification.code-expiry-minutes}")
    private long codeExpiryMinutes;

    public ApiResponse<AuthResponse> authenticateUser(LoginRequest loginRequest) {
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
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(user.toUserResponse())
                .build();
        
        return new ApiResponse<AuthResponse>(true, authResponse);
    }

    @Transactional
    public ApiResponse<String> registerUser(SignUpRequest signUpRequest) {
        // if user exists, and is enabled, return failed
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent() && existingUser.get().isEnabled()) {
            return new ApiResponse<String>(false, "Email is already taken!");
        }
        // user have not enabled, can continue sign up
        
        // Generate verification token
        // String emailVerificationToken = generateVerificationToken();
        // LocalDateTime tokenExpiry = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        // Generate verification code
        String verificationCode = generateVerificationCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(codeExpiryMinutes);
        
        // if user exit but not enabled, user can sign up again, update the verification code and code expiry
        User savedUser;
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFirstName(signUpRequest.getFirstName());
            user.setLastName(signUpRequest.getLastName());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRoles(new HashSet<>(Collections.singleton(Role.fromString(signUpRequest.getRole()))));
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(codeExpiry);
            user.setUpdatedAt(LocalDateTime.now());
            savedUser = userRepository.save(user);
        } else {
            // if user not exist, create a new user
            User user = User.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(new HashSet<>(Collections.singleton(Role.fromString(signUpRequest.getRole()))))
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                // .emailVerificationToken(emailVerificationToken)
                // .emailVerificationTokenExpiry(tokenExpiry)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(codeExpiry)
                .enabled(false) // User is not enabled until email is verified
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
            savedUser = userRepository.save(user);
        }
        
        // Send verification token email
        // emailService.sendVerificationEmail(
        //         savedUser.getEmail(),
        //         emailVerificationToken
        // );
        emailService.sendVerificationCode(
                savedUser.getEmail(),
                verificationCode
        );

        return new ApiResponse<String>(true, "User registered successfully. Please check your email to complete registration.");
    }

    @Transactional
    public ApiResponse<String> registerStaff(SignUpStaffRequest signUpStaffRequest) {
        
        Optional<User> existingUser = userRepository.findByEmail(signUpStaffRequest.getEmail());
        if (existingUser.isPresent() && existingUser.get().isEnabled()) {
            return new ApiResponse<String>(false, "Email is already taken!");
        }

        // Generate verification code
        String verificationCode = generateVerificationCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(codeExpiryMinutes);

        // if user exit but not enabled, user can sign up again, update the verification code and code expiry
        User savedUser;
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFirstName(signUpStaffRequest.getFirstName());
            user.setLastName(signUpStaffRequest.getLastName());
            user.setPassword(passwordEncoder.encode(signUpStaffRequest.getPassword()));
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(codeExpiry);
            user.setClinicId(signUpStaffRequest.getClinicId());
            user.setClinicName(signUpStaffRequest.getClinicName());
            user.setRoles(new HashSet<>(Collections.singleton(Role.fromString(signUpStaffRequest.getRole()))));
            user.setEmailVerified(false);
            user.setEnabled(false);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setUpdatedAt(LocalDateTime.now());
            savedUser = userRepository.save(user);
        } else {
            // if user not exist, create a new user
            User user = User.builder()
                .firstName(signUpStaffRequest.getFirstName())
                .lastName(signUpStaffRequest.getLastName())
                .email(signUpStaffRequest.getEmail())
                .password(passwordEncoder.encode(signUpStaffRequest.getPassword()))
                .provider(AuthProvider.LOCAL)
                .roles(new HashSet<>(Collections.singleton(Role.fromString(signUpStaffRequest.getRole()))))
                .emailVerified(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(codeExpiry)
                .enabled(false)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
            savedUser = userRepository.save(user);
        }

        // Send verification code email
        emailService.sendVerificationCode(
                savedUser.getEmail(),
                verificationCode
        );

        // Get the clinic admin
        Optional<Clinic> clinic = clinicRepository.findById(signUpStaffRequest.getClinicId());
        User clinicAdmin = clinic.get().getAdmin();

        // Register as a staff of a clinic require the clinic admin's proof. Send a processing reminder email to clinic admin
        emailService.sendProcessingReminderEmail(
            clinicAdmin,
            signUpStaffRequest.getFirstName(),
            signUpStaffRequest.getLastName(),
            signUpStaffRequest.getEmail(),
            signUpStaffRequest.getRole()
        );

        return new ApiResponse<String>(true, "Staff registered successfully");
    }

    @Transactional
    public ApiResponse<String> resendVerificationCode(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (!existingUser.isPresent()) {
            return new ApiResponse<String>(false, "User not found");
        }
        User user = existingUser.get();
        if (user.isEmailVerified()) {
            return new ApiResponse<String>(false, "Email already verified");
        }
        if (user.getVerificationCodeExpiry().isAfter(LocalDateTime.now())) {
            return new ApiResponse<String>(true, "Verification code already sent");
        }
        String verificationCode = generateVerificationCode();
        LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(codeExpiryMinutes);
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(codeExpiry);
        userRepository.save(user);
        emailService.sendVerificationCode(
                user.getEmail(),
                verificationCode
        );
        return new ApiResponse<String>(true, "Verification code sent successfully");
    }
    
    @Transactional
    public ApiResponse<AuthResponse> verifyEmailAndLogin(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (user.isEmailVerified()) {
            // User already verified, just log them in
            return new ApiResponse<AuthResponse>(true, authenticateAndGenerateToken(user));
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
        
        return new ApiResponse<AuthResponse>(true, authenticateAndGenerateToken(user));
    }
    
    private AuthResponse authenticateAndGenerateToken(User user) {
        // Create authentication token with UserPrincipal as principal to avoid ClassCastException
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null, // No credentials needed as we're authenticating directly
                principal.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(user.toUserResponse())
                .build();
    }
    
    private String generateVerificationCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public ApiResponse<String> verifyEmailByCode(String email, String code) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (!existingUser.isPresent()) {
            return new ApiResponse<String>(false, "User not found");
        }
        
        User user = existingUser.get();
        
        if (user.isEmailVerified()) {
            return new ApiResponse<String>(true, "Email already verified");
        }
        
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            return new ApiResponse<String>(false, "Invalid verification code");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (user.getVerificationCodeExpiry().isBefore(now)) {
            return new ApiResponse<String>(false, "Verification code has expired");
        }
        
        user.setEmailVerified(true);
        // only if user is patient and is not dentist, receptionist, clinic admin, set enabled to true
        if (user.getRoles().contains(Role.PATIENT) && user.getRoles().size() == 1) {
            user.setEnabled(true);
        }
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return new ApiResponse<String>(true, "Email verified successfully");
    }

    public ApiResponse<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ApiResponse<UserResponse>(false, null);
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResponse<UserResponse>(true, user.toUserResponse());
    }
    
    @Transactional
    public ApiResponse<AuthResponse> processOAuthLogin(OAuthLoginRequest oAuthLoginRequest) {
        Optional<User> userOptionalByProviderId = userRepository.findByProviderIdAndProvider(oAuthLoginRequest.getProviderId(), AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()));

        User user;
        if (userOptionalByProviderId.isPresent()) {
            // User found by provider ID, this is a returning OAuth user
            user = userOptionalByProviderId.get();
            // Optionally update first/last name if they changed in Google profile
            user.setFirstName(oAuthLoginRequest.getFirstName());
            user.setLastName(oAuthLoginRequest.getLastName());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        } else {
            // No user found by provider ID, check by email
            Optional<User> userOptionalByEmail = userRepository.findByEmail(oAuthLoginRequest.getEmail());
            if (userOptionalByEmail.isPresent()) {
                // User found by email, link this OAuth account to the existing local account
                user = userOptionalByEmail.get();
                user.setProvider(AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()));
                user.setProviderId(oAuthLoginRequest.getProviderId());
                // If names are empty in local but available from OAuth, set them
                if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                    user.setFirstName(oAuthLoginRequest.getFirstName());
                }
                if (user.getLastName() == null || user.getLastName().isEmpty()) {
                    user.setLastName(oAuthLoginRequest.getLastName());
                }
                user.setEmailVerified(true); // Email is verified by Google
                user.setEnabled(true); // Enable account
                user.setUpdatedAt(LocalDateTime.now());
                user = userRepository.save(user);
            } else {
                // New user, create an account
                user = User.builder()
                    .email(oAuthLoginRequest.getEmail())
                    .firstName(oAuthLoginRequest.getFirstName())
                    .lastName(oAuthLoginRequest.getLastName())
                    .provider(AuthProvider.valueOf(oAuthLoginRequest.getProvider().toUpperCase()))
                    .providerId(oAuthLoginRequest.getProviderId())
                    .roles(new HashSet<>(Collections.singleton(Role.PATIENT))) // Default role for new OAuth users
                    .emailVerified(true) // Email is verified by Google
                    .enabled(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .accountNonLocked(true)
                    .build();
                user = userRepository.save(user);
            }
        }

        // Authenticate and generate token for the user
        AuthResponse authResponse = authenticateAndGenerateToken(user);
        return new ApiResponse<>(true, authResponse);
    }

    @Transactional
    public ApiResponse<String> changePassword(ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ApiResponse<String>(false, "User not authenticated");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return new ApiResponse<String>(true, "Password changed successfully");
    }
}
