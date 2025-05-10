package press.mizhifei.dentist.auth.security.oauth2.user;

import java.util.Map;

/**
 * @author zhifeimi
 */
public class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {

    public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("givenName");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("surname");
    }
}
