package press.mizhifei.dentist.clinicalrecords.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClinicalNoteRequest {
    
    private Long appointmentId;
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Dentist ID is required")
    private Long dentistId;
    
    @NotNull(message = "Clinic ID is required")
    private Long clinicId;
    
    private Long visitId;
    
    @Size(max = 1000, message = "Chief complaint must not exceed 1000 characters")
    private String chiefComplaint;
    
    private String examinationFindings;
    
    private String diagnosis;
    
    private String treatmentPerformed;
    
    private String treatmentPlan;
    
    private String prescriptions;
    
    private String followUpInstructions;
    
    private String aiAssistedNotes;
    
    private String[] attachments;
    
    private String category; // ROUTINE, EMERGENCY, CONSULTATION, FOLLOW_UP
    
    @Builder.Default
    private Boolean isDraft = false;
    
    private Long parentNoteId; // For creating note revisions
}
