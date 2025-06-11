package com.dentistdss.auth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dentistdss.auth.dto.ApiResponse;
import com.dentistdss.auth.dto.AuthResponse;
import com.dentistdss.auth.dto.IdTokenRequest;
import com.dentistdss.auth.dto.OAuthLoginRequest;
import com.dentistdss.auth.service.OAuthUserService;

/**
 * OAuth2 Controller for handling OAuth2 authentication flows
 * Migrated from oauth-service to consolidate authentication functionality
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
public class OAuth2Controller {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final OAuthUserService oAuthUserService;

    /**
     * Authenticate user with Google ID token (for popup-based OAuth2 flows)
     * This endpoint handles the Google ID token verification and user authentication
     */
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

            // Process OAuth login directly through AuthService (no more Feign client needed)
            ApiResponse<AuthResponse> response = oAuthUserService.processOAuthLogin(oAuthLoginRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error verifying Google ID token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Authentication failed"));
        }
    }
}
