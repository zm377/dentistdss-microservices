package press.mizhifei.dentist.auth.security.oauth2.user;


import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import press.mizhifei.dentist.auth.model.AuthProvider;

import java.util.Map;

/**
 * @author zhifeimi
 */
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.MICROSOFT.toString())) {
            return new MicrosoftOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.APPLE.toString())) {
            return new AppleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported yet.");
        }
    }
}
