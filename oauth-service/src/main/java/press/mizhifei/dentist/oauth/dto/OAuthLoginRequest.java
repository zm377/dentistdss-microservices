package press.mizhifei.dentist.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthLoginRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String provider; // e.g., "GOOGLE", "FACEBOOK"
    private String providerId;
} 