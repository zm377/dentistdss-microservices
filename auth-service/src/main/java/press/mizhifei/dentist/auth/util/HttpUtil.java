package press.mizhifei.dentist.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class HttpUtil {


//    getJwtFromRequest
    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

