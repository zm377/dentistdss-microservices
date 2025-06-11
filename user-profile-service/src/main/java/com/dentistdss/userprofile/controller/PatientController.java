package com.dentistdss.userprofile.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dentistdss.userprofile.dto.ApiResponse;
import com.dentistdss.userprofile.dto.PatientRequest;
import com.dentistdss.userprofile.dto.PatientResponse;
import com.dentistdss.userprofile.service.PatientService;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(@RequestBody PatientRequest request) {
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> listAllPatients() {
        List<PatientResponse> patients = patientService.listAllPatients();
        return ResponseEntity.ok(ApiResponse.success(patients));
    }
} 