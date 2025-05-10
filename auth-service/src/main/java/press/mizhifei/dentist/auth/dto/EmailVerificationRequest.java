package press.mizhifei.dentist.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhifeimi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationRequest {

    @NotBlank(message = "Verification token is required")
    private String token;
} 