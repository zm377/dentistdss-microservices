package press.mizhifei.dentist.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthLoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String firstName; // Optional, but good to have

    private String lastName; // Optional, but good to have

    @NotBlank(message = "Provider is required")
    private String provider; // e.g., "GOOGLE"

    @NotBlank(message = "Provider ID is required")
    private String providerId;
} 