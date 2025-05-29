package press.mizhifei.dentist.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.service.UserService;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/email")
    public String getUserEmail(@PathVariable Long id) {
        return userService.getUserEmail(id);
    }

    @GetMapping("/{id}/name")
    public String getUserFullName(@PathVariable Long id) {
        return userService.getUserFullName(id);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    public static class UserDetailsResponse {
        public Long id;
        public String email;
        public String firstName;
        public String lastName;
        public String fullName;
        public String phone;
        public String address;

        public UserDetailsResponse(Long id, String email, String firstName, String lastName,
                String phone, String address) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.phone = phone;
            this.address = address;
        }
    }
}