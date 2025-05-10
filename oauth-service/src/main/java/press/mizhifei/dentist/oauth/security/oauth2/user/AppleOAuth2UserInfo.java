package press.mizhifei.dentist.oauth.security.oauth2.user;

import java.util.Map;

/**
 * @author zhifeimi
 */
public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getFirstName() {
        // Apple doesn't provide first name directly
        return "";
    }

    @Override
    public String getLastName() {
        // Apple doesn't provide last name directly
        return "";
    }
} 