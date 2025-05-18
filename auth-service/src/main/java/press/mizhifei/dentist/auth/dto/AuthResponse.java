package press.mizhifei.dentist.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.auth.model.Role;

import java.util.Set;

/**
 * @author zhifeimi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private UserResponse user;
}
