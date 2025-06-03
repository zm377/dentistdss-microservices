package press.mizhifei.dentist.clinic.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider for clinic service
 * Used to extract information from JWT tokens
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * Extracts the email from a JWT token
     * @param token the JWT token
     * @return the email
     */
    public String getEmailFromJWT(String token) {
        try {
            // Parse the JWT token without verification since we're in a different service
            // The token has already been verified by Spring Security
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            
            // Parse JSON manually to extract email
            // This is a simple approach - in production you might want to use a JSON library
            if (payload.contains("\"email\":")) {
                String emailPart = payload.substring(payload.indexOf("\"email\":") + 8);
                emailPart = emailPart.substring(emailPart.indexOf("\"") + 1);
                emailPart = emailPart.substring(0, emailPart.indexOf("\""));
                return emailPart;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting email from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the roles from a JWT token
     * @param token the JWT token
     * @return the roles as a comma-separated string
     */
    public String getRolesFromJWT(String token) {
        try {
            // Parse the JWT token without verification since we're in a different service
            // The token has already been verified by Spring Security
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            
            // Parse JSON manually to extract roles
            if (payload.contains("\"roles\":")) {
                String rolesPart = payload.substring(payload.indexOf("\"roles\":") + 8);
                rolesPart = rolesPart.substring(rolesPart.indexOf("\"") + 1);
                rolesPart = rolesPart.substring(0, rolesPart.indexOf("\""));
                return rolesPart;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting roles from JWT: {}", e.getMessage());
            return null;
        }
    }
}
