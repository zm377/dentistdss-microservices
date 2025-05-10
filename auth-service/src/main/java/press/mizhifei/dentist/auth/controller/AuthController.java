package press.mizhifei.dentist.auth.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.AuthResponse;
import press.mizhifei.dentist.auth.dto.LoginRequest;
import press.mizhifei.dentist.auth.dto.SignUpRequest;
import press.mizhifei.dentist.auth.service.AuthService;

/**
 * @author zhifeimi
 */

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Environment environment;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @GetMapping("/oauth2/google")
    public RedirectView redirectToGoogleOAuth() {
        // This endpoint will redirect to Google's OAuth2 authorization URL
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String redirectUri = environment.getProperty("app.oauth2.authorized-redirect-uri");
        
        // Construct the Google OAuth2 URL
        String googleOAuthUrl = "https://accounts.google.com/o/oauth2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=email%20profile" +
                "&access_type=offline";
        
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(googleOAuthUrl);
        return redirectView;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser() {
        // JWT is stateless, so we don't need to do anything on the server side
        // The client should remove the token from local storage
        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        return ResponseEntity.ok(new ApiResponse(true, "User is authenticated"));
    }


}
