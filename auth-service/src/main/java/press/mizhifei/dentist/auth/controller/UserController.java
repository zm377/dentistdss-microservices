package press.mizhifei.dentist.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.auth.annotation.RequireRoles;
import press.mizhifei.dentist.auth.dto.ApiResponse;
import press.mizhifei.dentist.auth.dto.UserResponse;
import press.mizhifei.dentist.auth.dto.UserUpdateRequest;
import press.mizhifei.dentist.auth.model.Role;
import press.mizhifei.dentist.auth.model.User;
import press.mizhifei.dentist.auth.security.JwtTokenProvider;
import press.mizhifei.dentist.auth.service.UserService;

import java.util.List;

import static press.mizhifei.dentist.auth.util.HttpUtil.getJwtFromRequest;

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

    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/list/all")
    @RequireRoles({Role.SYSTEM_ADMIN, Role.CLINIC_ADMIN})
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAllUsers(HttpServletRequest request) {

        String jwt = getJwtFromRequest(request);

        String userEmail = jwtTokenProvider.getEmailFromJWT(jwt);
        String rolesString = jwtTokenProvider.getRolesFromJWT(jwt);
        if (!StringUtils.hasText(userEmail) || !StringUtils.hasText(rolesString)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid authentication token"));
        }
        List<String> userRoles = List.of(rolesString.split(","));

        boolean hasClinicAdmin = userRoles.contains(Role.CLINIC_ADMIN.name());
        boolean hasSystemAdmin = userRoles.contains(Role.SYSTEM_ADMIN.name());

        if (!hasSystemAdmin && !hasClinicAdmin) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You are not a legal administrator"));
        }
        List<UserResponse> users;
        if (hasClinicAdmin) {
            users = userService.listClinicUsers(userEmail);
        } else {
            users = userService.listAllUsers();
        }
        return ResponseEntity.ok(ApiResponse.success(users));
    }

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

    @GetMapping("/email/{email}/details")
    public UserDetailsResponse getUserDetailsByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return new UserDetailsResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
                user.getClinicId(),
                user.getClinicName(),
                user.isEnabled()
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        ApiResponse<UserResponse> response = userService.updateUserProfile(userId, updateRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clinic/{clinicId}/dentists")
    public List<UserResponse> getClinicDentists(@PathVariable Long clinicId) {
        return userService.getClinicDentists(clinicId);
    }

    public static class UserDetailsResponse {
        public Long id;
        public String email;
        public String firstName;
        public String lastName;
        public String fullName;
        public String phone;
        public String address;
        public java.util.Set<String> roles;
        public Long clinicId;
        public String clinicName;
        public Boolean enabled;

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

        public UserDetailsResponse(Long id, String email, String firstName, String lastName,
                String phone, String address, java.util.Set<String> roles, Long clinicId,
                String clinicName, Boolean enabled) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.phone = phone;
            this.address = address;
            this.roles = roles;
            this.clinicId = clinicId;
            this.clinicName = clinicName;
            this.enabled = enabled;
        }
    }
}