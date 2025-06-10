package press.mizhifei.dentist.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationEmailRequest {
    private String to;
    private String verificationValue;
    private String type; // "token" or "code"
}
