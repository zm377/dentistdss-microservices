package press.mizhifei.dentist.clinicadmin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import press.mizhifei.dentist.clinicadmin.dto.UserDetailsResponse;
import press.mizhifei.dentist.clinicadmin.dto.UserResponse;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

    @GetMapping("/user/{id}/name")
    String getUserFullName(@PathVariable("id") Long userId);

    @GetMapping("/user/email/{email}/details")
    UserDetailsResponse getUserDetailsByEmail(@PathVariable("email") String email);

    @GetMapping("/user/clinic/{clinicId}/dentists")
    List<UserResponse> getClinicDentists(@PathVariable("clinicId") Long clinicId);
}