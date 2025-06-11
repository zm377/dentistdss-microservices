package com.dentistdss.clinicadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.clinicadmin.client.AppointmentServiceClient;
import com.dentistdss.clinicadmin.client.AuthServiceClient;
import com.dentistdss.clinicadmin.client.PatientServiceClient;
import com.dentistdss.clinicadmin.dto.*;
import com.dentistdss.clinicadmin.model.Clinic;
import com.dentistdss.clinicadmin.repository.ClinicRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced Clinic Administration Service
 * 
 * Provides comprehensive clinic management capabilities with proper
 * separation of concerns and SOLID principles implementation
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicAdminService {

    private final ClinicRepository clinicRepository;
    private final PatientServiceClient patientServiceClient;
    private final AppointmentServiceClient appointmentServiceClient;
    private final AuthServiceClient authServiceClient;

    /**
     * Get all enabled clinics for public listing
     */
    public List<ClinicResponse> listAllEnabledClinics() {
        log.debug("Fetching all enabled clinics");
        List<Clinic> clinics = clinicRepository.findByEnabledTrueAndApprovedTrueOrderByNameAsc();
        return clinics.stream()
                .map(this::mapToClinicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get clinic by ID
     */
    public ClinicResponse getClinicById(Long id) {
        log.debug("Fetching clinic with ID: {}", id);
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + id));
        return mapToClinicResponse(clinic);
    }

    /**
     * Search clinics based on criteria
     */
    public List<ClinicResponse> searchClinics(ClinicSearchRequest request) {
        log.debug("Searching clinics with criteria: {}", request);
        
        List<Clinic> clinics;
        if (request.getKeywords() != null && !request.getKeywords().trim().isEmpty()) {
            clinics = clinicRepository.searchEnabledClinics(request.getKeywords().trim());
        } else {
            clinics = clinicRepository.findByEnabledTrueAndApprovedTrueOrderByNameAsc();
        }

        return clinics.stream()
                .map(this::mapToClinicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new clinic
     */
    @Transactional
    public ClinicResponse createClinic(ClinicCreateRequest request) {
        log.info("Creating new clinic: {}", request.getName());
        
        // Check if clinic name already exists
        if (clinicRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Clinic with name '" + request.getName() + "' already exists");
        }

        Clinic clinic = Clinic.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .description(request.getDescription())
                .timeZone(request.getTimeZone())
                .enabled(false) // New clinics start disabled
                .approved(false) // Require approval
                .build();

        Clinic savedClinic = clinicRepository.save(clinic);
        log.info("Created clinic with ID: {}", savedClinic.getId());
        
        return mapToClinicResponse(savedClinic);
    }

    /**
     * Update clinic information (restricted to clinic admins)
     */
    @Transactional
    public ClinicResponse updateClinic(Long id, ClinicUpdateRequest request) {
        log.info("Updating clinic with ID: {}", id);
        
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + id));

        // Check if name is being changed and if it conflicts
        if (request.getName() != null && !request.getName().equalsIgnoreCase(clinic.getName())) {
            if (clinicRepository.existsByNameIgnoreCase(request.getName())) {
                throw new IllegalArgumentException("Clinic with name '" + request.getName() + "' already exists");
            }
            clinic.setName(request.getName());
        }

        // Update other fields if provided
        if (request.getAddress() != null) clinic.setAddress(request.getAddress());
        if (request.getCity() != null) clinic.setCity(request.getCity());
        if (request.getState() != null) clinic.setState(request.getState());
        if (request.getZipCode() != null) clinic.setZipCode(request.getZipCode());
        if (request.getCountry() != null) clinic.setCountry(request.getCountry());
        if (request.getPhoneNumber() != null) clinic.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) clinic.setEmail(request.getEmail());

        Clinic updatedClinic = clinicRepository.save(clinic);
        log.info("Updated clinic with ID: {}", updatedClinic.getId());
        
        return mapToClinicResponse(updatedClinic);
    }

    /**
     * Approve clinic (system admin only)
     */
    @Transactional
    public ClinicResponse approveClinic(Long id) {
        log.info("Approving clinic with ID: {}", id);
        
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found with ID: " + id));

        clinic.setApproved(true);
        clinic.setEnabled(true); // Enable when approved
        
        Clinic approvedClinic = clinicRepository.save(clinic);
        log.info("Approved clinic with ID: {}", approvedClinic.getId());
        
        return mapToClinicResponse(approvedClinic);
    }

    /**
     * Get clinic patients sorted by appointments
     */
    public List<PatientWithAppointmentResponse> getClinicPatientsSortedByAppointments(Long clinicId) {
        log.debug("Fetching patients for clinic ID: {}", clinicId);
        
        // Verify clinic exists
        if (!clinicRepository.existsById(clinicId)) {
            throw new IllegalArgumentException("Clinic not found with ID: " + clinicId);
        }

        try {
            // For now, return empty list - this would be implemented when patient service is available
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching patients for clinic {}: {}", clinicId, e.getMessage());
            throw new RuntimeException("Failed to fetch clinic patients", e);
        }
    }

    /**
     * Get clinic dentists
     */
    public List<UserResponse> getClinicDentists(Long clinicId) {
        log.debug("Fetching dentists for clinic ID: {}", clinicId);
        
        // Verify clinic exists
        if (!clinicRepository.existsById(clinicId)) {
            throw new IllegalArgumentException("Clinic not found with ID: " + clinicId);
        }

        try {
            // For now, return empty list - this would be implemented when auth service integration is available
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching dentists for clinic {}: {}", clinicId, e.getMessage());
            throw new RuntimeException("Failed to fetch clinic dentists", e);
        }
    }

    /**
     * Map Clinic entity to ClinicResponse DTO
     */
    private ClinicResponse mapToClinicResponse(Clinic clinic) {
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .city(clinic.getCity())
                .state(clinic.getState())
                .zipCode(clinic.getZipCode())
                .country(clinic.getCountry())
                .phoneNumber(clinic.getPhoneNumber())
                .email(clinic.getEmail())
                .website(clinic.getWebsite())
                .build();
    }
}
