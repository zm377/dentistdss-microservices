package press.mizhifei.dentist.clinic.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Utility class for JWT token handling
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
public class JwtUtil {
    
    /**
     * Extracts JWT token from HTTP request Authorization header
     * 
     * @param request the HTTP request
     * @return the JWT token without "Bearer " prefix, or null if not found
     */
    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
