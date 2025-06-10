package press.mizhifei.dentist.userprofile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.userprofile.dto.ApiResponse;
import press.mizhifei.dentist.userprofile.dto.UserResponse;
import press.mizhifei.dentist.userprofile.service.DentistService;

import java.util.List;

/**
 * Controller for dentist-specific profile management
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/dentist")
@RequiredArgsConstructor
public class DentistController {

    private final DentistService dentistService;

    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAllDentists() {
        List<UserResponse> dentists = dentistService.listAllDentists();
        return ResponseEntity.ok(ApiResponse.success(dentists));
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDentistsByClinic(@PathVariable Long clinicId) {
        List<UserResponse> dentists = dentistService.getDentistsByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success(dentists));
    }

    @GetMapping("/{dentistId}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getDentistProfile(@PathVariable Long dentistId) {
        UserResponse dentist = dentistService.getDentistProfile(dentistId);
        return ResponseEntity.ok(ApiResponse.success(dentist));
    }
}
