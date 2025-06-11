package press.mizhifei.dentist.clinicadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.clinicadmin.model.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * User response DTO for clinic service
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
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String profilePictureUrl;
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    private Long clinicId;
    private String clinicName;
    private Boolean enabled;
}
