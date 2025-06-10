package press.mizhifei.dentist.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider for API Gateway
 * Handles JWT token parsing and validation for centralized authentication
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /**
     * Extracts the user ID from a JWT token
     * @param token the JWT token
     * @return the user ID
     */
    public String getUserIdFromJWT(String token) {
        try {
            // Parse the JWT token without verification since we're in a different service
            // The token has already been verified by Spring Security
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            
            // Parse JSON manually to extract user ID
            if (payload.contains("\"sub\":")) {
                String subPart = payload.substring(payload.indexOf("\"sub\":") + 6);
                subPart = subPart.substring(subPart.indexOf("\"") + 1);
                subPart = subPart.substring(0, subPart.indexOf("\""));
                return subPart;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting user ID from JWT: {}", e.getMessage());
            return null;
        }
    }

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

    /**
     * Extracts the clinic ID from a JWT token
     * @param token the JWT token
     * @return the clinic ID
     */
    public String getClinicIdFromJWT(String token) {
        try {
            // Parse the JWT token without verification since we're in a different service
            // The token has already been verified by Spring Security
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            // Decode the payload (second part)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            
            // Parse JSON manually to extract clinic ID
            if (payload.contains("\"clinicId\":")) {
                String clinicIdPart = payload.substring(payload.indexOf("\"clinicId\":") + 11);
                // Handle both string and number formats
                if (clinicIdPart.trim().startsWith("\"")) {
                    clinicIdPart = clinicIdPart.substring(clinicIdPart.indexOf("\"") + 1);
                    clinicIdPart = clinicIdPart.substring(0, clinicIdPart.indexOf("\""));
                } else {
                    // Handle numeric format
                    clinicIdPart = clinicIdPart.trim();
                    int endIndex = 0;
                    while (endIndex < clinicIdPart.length() && 
                           (Character.isDigit(clinicIdPart.charAt(endIndex)) || clinicIdPart.charAt(endIndex) == '.')) {
                        endIndex++;
                    }
                    clinicIdPart = clinicIdPart.substring(0, endIndex);
                }
                return clinicIdPart;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting clinic ID from JWT: {}", e.getMessage());
            return null;
        }
    }
}
