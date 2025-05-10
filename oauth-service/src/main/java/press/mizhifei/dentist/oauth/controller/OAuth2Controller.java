package press.mizhifei.dentist.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import press.mizhifei.dentist.oauth.dto.ApiResponse;

/**
 * @author zhifeimi
 */
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final Environment environment;

    @GetMapping("/google")
    public RedirectView redirectToGoogleOAuth() {
        // This endpoint will redirect to Google's OAuth2 authorization URL
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String redirectUri = environment.getProperty("app.oauth2.authorized-redirect-uris[0]");
        
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

    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getStatus() {
        return ResponseEntity.ok(new ApiResponse(true, "OAuth2 Service is running"));
    }
} 