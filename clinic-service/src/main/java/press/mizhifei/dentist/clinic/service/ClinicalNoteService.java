package press.mizhifei.dentist.clinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.clinic.client.AuthServiceClient;
import press.mizhifei.dentist.clinic.dto.ClinicalNoteRequest;
import press.mizhifei.dentist.clinic.dto.ClinicalNoteResponse;
import press.mizhifei.dentist.clinic.model.ClinicalNote;
import press.mizhifei.dentist.clinic.repository.AppointmentRepository;
import press.mizhifei.dentist.clinic.repository.ClinicRepository;
import press.mizhifei.dentist.clinic.repository.ClinicalNoteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalNoteService {
    
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final AuthServiceClient authServiceClient;
    
    @Transactional
    public ClinicalNoteResponse createClinicalNote(ClinicalNoteRequest request) {
        // Validate appointment exists if provided
        if (request.getAppointmentId() != null) {
            appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        }
        
        ClinicalNote clinicalNote = ClinicalNote.builder()
                .appointmentId(request.getAppointmentId())
                .patientId(request.getPatientId())
                .dentistId(request.getDentistId())
                .clinicId(request.getClinicId())
                .chiefComplaint(request.getChiefComplaint())
                .examinationFindings(request.getExaminationFindings())
                .diagnosis(request.getDiagnosis())
                .treatmentPerformed(request.getTreatmentPerformed())
                .treatmentPlan(request.getTreatmentPlan())
                .prescriptions(request.getPrescriptions())
                .followUpInstructions(request.getFollowUpInstructions())
                .aiAssistedNotes(request.getAiAssistedNotes())
                .attachments(request.getAttachments())
                .isDraft(request.getIsDraft())
                .build();
        
        ClinicalNote saved = clinicalNoteRepository.save(clinicalNote);
        log.info("Created clinical note {} for patient {} by dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ClinicalNoteResponse updateClinicalNote(Long noteId, ClinicalNoteRequest request) {
        ClinicalNote clinicalNote = clinicalNoteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical note not found"));
        
        if (!clinicalNote.getIsDraft()) {
            throw new IllegalStateException("Cannot update a signed clinical note");
        }
        
        clinicalNote.setChiefComplaint(request.getChiefComplaint());
        clinicalNote.setExaminationFindings(request.getExaminationFindings());
        clinicalNote.setDiagnosis(request.getDiagnosis());
        clinicalNote.setTreatmentPerformed(request.getTreatmentPerformed());
        clinicalNote.setTreatmentPlan(request.getTreatmentPlan());
        clinicalNote.setPrescriptions(request.getPrescriptions());
        clinicalNote.setFollowUpInstructions(request.getFollowUpInstructions());
        clinicalNote.setAiAssistedNotes(request.getAiAssistedNotes());
        clinicalNote.setAttachments(request.getAttachments());
        clinicalNote.setIsDraft(request.getIsDraft());
        
        ClinicalNote saved = clinicalNoteRepository.save(clinicalNote);
        log.info("Updated clinical note {}", noteId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public ClinicalNoteResponse signClinicalNote(Long noteId, Long signedBy) {
        ClinicalNote clinicalNote = clinicalNoteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical note not found"));
        
        if (!clinicalNote.getIsDraft()) {
            throw new IllegalStateException("Clinical note is already signed");
        }
        
        if (!clinicalNote.getDentistId().equals(signedBy)) {
            throw new IllegalStateException("Only the creating dentist can sign this note");
        }
        
        clinicalNote.setIsDraft(false);
        clinicalNote.setSignedAt(LocalDateTime.now());
        clinicalNote.setSignedBy(signedBy);
        
        ClinicalNote saved = clinicalNoteRepository.save(clinicalNote);
        log.info("Signed clinical note {} by dentist {}", noteId, signedBy);
        
        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public ClinicalNoteResponse getClinicalNote(Long noteId) {
        ClinicalNote clinicalNote = clinicalNoteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical note not found"));
        
        return toResponse(clinicalNote);
    }

    // getClinicClinicalNotes

    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getClinicClinicalNotes(Long clinicId) {
        List<ClinicalNote> notes;

        notes = clinicalNoteRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);

        return notes.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getPatientClinicalNotes(Long patientId, boolean includeDrafts) {
        List<ClinicalNote> notes;
        if (includeDrafts) {
            notes = clinicalNoteRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        } else {
            notes = clinicalNoteRepository.findSignedNotesByPatientId(patientId);
        }
        
        return notes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getDentistClinicalNotes(Long dentistId) {
        List<ClinicalNote> notes;
        notes = clinicalNoteRepository.findByDentistIdOrderByCreatedAtDesc(dentistId);
        return notes.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> getDentistDraftNotes(Long dentistId) {
        List<ClinicalNote> notes = clinicalNoteRepository.findDraftNotesByDentistId(dentistId);
        return notes.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ClinicalNoteResponse getClinicalNoteByAppointment(Long appointmentId) {
        ClinicalNote clinicalNote = clinicalNoteRepository.findByAppointmentId(appointmentId)
                .orElse(null);
        
        return clinicalNote != null ? toResponse(clinicalNote) : null;
    }
    
    private ClinicalNoteResponse toResponse(ClinicalNote clinicalNote) {
        ClinicalNoteResponse response = ClinicalNoteResponse.builder()
                .id(clinicalNote.getId())
                .appointmentId(clinicalNote.getAppointmentId())
                .patientId(clinicalNote.getPatientId())
                .dentistId(clinicalNote.getDentistId())
                .clinicId(clinicalNote.getClinicId())
                .chiefComplaint(clinicalNote.getChiefComplaint())
                .examinationFindings(clinicalNote.getExaminationFindings())
                .diagnosis(clinicalNote.getDiagnosis())
                .treatmentPerformed(clinicalNote.getTreatmentPerformed())
                .treatmentPlan(clinicalNote.getTreatmentPlan())
                .prescriptions(clinicalNote.getPrescriptions())
                .followUpInstructions(clinicalNote.getFollowUpInstructions())
                .aiAssistedNotes(clinicalNote.getAiAssistedNotes())
                .attachments(clinicalNote.getAttachments())
                .isDraft(clinicalNote.getIsDraft())
                .createdAt(clinicalNote.getCreatedAt())
                .updatedAt(clinicalNote.getUpdatedAt())
                .signedAt(clinicalNote.getSignedAt())
                .signedBy(clinicalNote.getSignedBy())
                .build();
        
        // Fetch names from user service
        try {
            response.setPatientName(authServiceClient.getUserFullName(clinicalNote.getPatientId()));
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for id {}: {}", clinicalNote.getPatientId(), e.getMessage());
        }
        
        try {
            response.setDentistName(authServiceClient.getUserFullName(clinicalNote.getDentistId()));
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for id {}: {}", clinicalNote.getDentistId(), e.getMessage());
        }
        
        if (clinicalNote.getSignedBy() != null) {
            try {
                response.setSignedByName(authServiceClient.getUserFullName(clinicalNote.getSignedBy()));
            } catch (Exception e) {
                log.warn("Failed to fetch signer name for id {}: {}", clinicalNote.getSignedBy(), e.getMessage());
            }
        }
        
        // Fetch clinic name
        clinicRepository.findById(clinicalNote.getClinicId()).ifPresent(clinic -> 
                response.setClinicName(clinic.getName())
        );
        
        return response;
    }
} 