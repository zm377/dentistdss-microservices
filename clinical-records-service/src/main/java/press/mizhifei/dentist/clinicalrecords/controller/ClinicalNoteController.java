package press.mizhifei.dentist.clinicalrecords.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import press.mizhifei.dentist.clinicalrecords.dto.ApiResponse;
import press.mizhifei.dentist.clinicalrecords.dto.ClinicalNoteRequest;
import press.mizhifei.dentist.clinicalrecords.dto.ClinicalNoteResponse;
import press.mizhifei.dentist.clinicalrecords.service.ClinicalNoteService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinical-records/note")
@RequiredArgsConstructor
public class ClinicalNoteController {
    
    private final ClinicalNoteService clinicalNoteService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> createClinicalNote(
            @Valid @RequestBody ClinicalNoteRequest request) {
        ClinicalNoteResponse response = clinicalNoteService.createClinicalNote(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> updateClinicalNote(
            @PathVariable Long id,
            @Valid @RequestBody ClinicalNoteRequest request) {
        ClinicalNoteResponse response = clinicalNoteService.updateClinicalNote(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/sign")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> signClinicalNote(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null || userId == 0) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Authentication required"));
        }
        ClinicalNoteResponse response = clinicalNoteService.signClinicalNote(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> getClinicalNote(@PathVariable Long id) {
        ClinicalNoteResponse response = clinicalNoteService.getClinicalNote(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getClinicClinicalNotes(
            @PathVariable Long clinicId) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getClinicClinicalNotes(clinicId);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getPatientClinicalNotes(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "false") boolean includeDrafts) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getPatientClinicalNotes(patientId, includeDrafts);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/patient/{patientId}/category/{category}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getPatientNotesByCategory(
            @PathVariable Long patientId,
            @PathVariable String category) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getPatientNotesByCategory(patientId, category);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/patient/{patientId}/search")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> searchPatientNotes(
            @PathVariable Long patientId,
            @RequestParam String searchTerm) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.searchPatientNotes(patientId, searchTerm);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/dentist/{dentistId}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getDentistClinicalNotes(
            @PathVariable Long dentistId) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getDentistClinicalNotes(dentistId);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/dentist/{dentistId}/drafts")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getDentistDraftNotes(
            @PathVariable Long dentistId) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getDentistDraftNotes(dentistId);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> getClinicalNoteByAppointment(
            @PathVariable Long appointmentId) {
        ClinicalNoteResponse response = clinicalNoteService.getClinicalNoteByAppointment(appointmentId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.error("No clinical note found for this appointment"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getVisitClinicalNotes(
            @PathVariable Long visitId) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getVisitClinicalNotes(visitId);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
    
    @GetMapping("/{parentNoteId}/versions")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> getNoteVersions(
            @PathVariable Long parentNoteId) {
        List<ClinicalNoteResponse> notes = clinicalNoteService.getNoteVersions(parentNoteId);
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
}
