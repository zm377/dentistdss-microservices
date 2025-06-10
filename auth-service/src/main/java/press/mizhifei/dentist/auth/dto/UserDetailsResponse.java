package press.mizhifei.dentist.auth.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {
    public Long id;
    public String email;
    public String firstName;
    public String lastName;
    public String fullName;
    public String phone;
    public String address;
    public Set<String> roles;
    public Long clinicId;
    public String clinicName;
    public Boolean enabled;
}
