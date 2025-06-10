package press.mizhifei.dentist.auth.dto;

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
public class SystemAdminApprovalEmailRequest {
    private String systemAdminEmail;
    private String firstName;
    private String lastName;
    private String email;
    private String clinicName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String phoneNumber;
    private String businessEmail;
}
