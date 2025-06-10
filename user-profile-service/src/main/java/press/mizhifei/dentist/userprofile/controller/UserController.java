package press.mizhifei.dentist.userprofile.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.userprofile.dto.ApiResponse;
import press.mizhifei.dentist.userprofile.dto.UserResponse;
import press.mizhifei.dentist.userprofile.dto.UserUpdateRequest;
import press.mizhifei.dentist.userprofile.model.User;
import press.mizhifei.dentist.userprofile.service.UserService;
import press.mizhifei.dentist.userprofile.service.UserService.UserDetailsResponse;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAllUsers(HttpServletRequest request) {
        // For now, simplified without JWT validation - you may want to add security later
        List<UserResponse> users = userService.listAllUsers();
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
}
