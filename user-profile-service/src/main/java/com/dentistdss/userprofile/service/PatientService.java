package com.dentistdss.userprofile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.userprofile.dto.PatientRequest;
import com.dentistdss.userprofile.dto.PatientResponse;
import com.dentistdss.userprofile.model.Patient;
import com.dentistdss.userprofile.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        Patient patient = Patient.builder()
                .id(request.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .healthHistory(request.getHealthHistory())
                .build();
        Patient saved = patientRepository.save(patient);
        return convertToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PatientResponse convertToDto(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .dateOfBirth(patient.getDateOfBirth())
                .email(patient.getEmail())
                .phoneNumber(patient.getPhoneNumber())
                .address(patient.getAddress())
                .healthHistory(patient.getHealthHistory())
                .build();
    }
} 