package press.mizhifei.dentist.auth.dto;

import jakarta.validation.constraints.Email;
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
public class VerifyCodeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Verification code is required")
    private String code;
} 