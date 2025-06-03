package press.mizhifei.dentist.oauth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import press.mizhifei.dentist.oauth.client.AuthServiceFeignClient;
import press.mizhifei.dentist.oauth.dto.ApiResponse;
import press.mizhifei.dentist.oauth.dto.AuthResponse;
import press.mizhifei.dentist.oauth.dto.IdTokenRequest;
import press.mizhifei.dentist.oauth.dto.OAuthLoginRequest;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuthPopupController {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final AuthServiceFeignClient authServiceFeignClient;

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithGoogleIdToken(
            @RequestBody IdTokenRequest request) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(request.getIdToken());
            if (idToken == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (payload == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid token payload"));
            }

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String googleId = payload.getSubject();

            OAuthLoginRequest oAuthLoginRequest = OAuthLoginRequest.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .provider("GOOGLE")
                    .providerId(googleId)
                    .build();

            ResponseEntity<ApiResponse<AuthResponse>> response = authServiceFeignClient.processOAuthLogin(oAuthLoginRequest);
            AuthResponse authResponse = response.getBody().getDataObject();
            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (Exception e) {
            log.error("Error verifying Google ID token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication failed"));
        }
    }
}