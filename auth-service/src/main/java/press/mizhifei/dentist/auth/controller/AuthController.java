package press.mizhifei.dentist.auth.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.AuthResponse;
import press.mizhifei.dentist.auth.dto.ChangePasswordRequest;
import press.mizhifei.dentist.auth.dto.LoginRequest;
import press.mizhifei.dentist.auth.dto.OAuthLoginRequest;
import press.mizhifei.dentist.auth.dto.SignUpClinicAdminRequest;
import press.mizhifei.dentist.auth.dto.SignUpRequest;
import press.mizhifei.dentist.auth.dto.SignUpStaffRequest;
import press.mizhifei.dentist.auth.dto.UserResponse;
import press.mizhifei.dentist.auth.dto.VerifyCodeRequest;
import press.mizhifei.dentist.auth.service.AuthService;
import press.mizhifei.dentist.auth.service.OAuthUserService;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthUserService oAuthUserService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/oauth/process")
    public ResponseEntity<ApiResponse<AuthResponse>> processOAuthLogin(@Valid @RequestBody OAuthLoginRequest oAuthLoginRequest) {
        return ResponseEntity.ok(oAuthUserService.processOAuthLogin(oAuthLoginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    @PostMapping("/signup/clinic/staff")
    public ResponseEntity<ApiResponse<String>> registerStaff(@Valid @RequestBody SignUpStaffRequest signUpStaffRequest) {
        return ResponseEntity.ok(authService.registerStaff(signUpStaffRequest));
    }

    @PostMapping("/signup/clinic/admin")
    public ResponseEntity<ApiResponse<String>> registerClinicAdmin(@Valid @RequestBody SignUpClinicAdminRequest signUpClinicAdminRequest) {
        return ResponseEntity.ok(authService.registerClinicAdmin(signUpClinicAdminRequest));
    }

    @PostMapping("/signup/verify/code/resend")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.resendVerificationCode(email));
    }

    @GetMapping("/signup/verify/token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@RequestParam("vtoken") String token) {
        return ResponseEntity.ok(authService.verifyEmailAndLogin(token));
    }

    @PostMapping("/signup/verify/code")
    public ResponseEntity<ApiResponse<String>> verifyEmailWithCode(@Valid @RequestBody VerifyCodeRequest verifyRequest) {
        return ResponseEntity.ok(authService.verifyEmailByCode(verifyRequest.getEmail(), verifyRequest.getCode()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser() {
        // JWT is stateless, so we don't need to do anything on the server side
        // The client should remove the token from local storage
        return ResponseEntity.ok(ApiResponse.successMessage("User logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(authService.changePassword(changePasswordRequest));
    }
}
