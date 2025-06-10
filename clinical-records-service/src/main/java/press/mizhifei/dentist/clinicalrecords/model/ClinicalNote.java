package press.mizhifei.dentist.clinicalrecords.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clinical_notes")
public class ClinicalNote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinical_note_seq")
    @SequenceGenerator(name = "clinical_note_seq", sequenceName = "patient_record_id_seq", allocationSize = 1, initialValue = 5000)
    private Long id;
    
    @Column(name = "appointment_id")
    private Long appointmentId;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "dentist_id", nullable = false)
    private Long dentistId;
    
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;
    
    @Column(name = "visit_id")
    private Long visitId; // New field for service visit tracking
    
    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;
    
    @Column(name = "examination_findings", columnDefinition = "TEXT")
    private String examinationFindings;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(name = "treatment_performed", columnDefinition = "TEXT")
    private String treatmentPerformed;
    
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;
    
    @Column(columnDefinition = "TEXT")
    private String prescriptions;
    
    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;
    
    @Column(name = "ai_assisted_notes", columnDefinition = "TEXT")
    private String aiAssistedNotes;
    
    @Column(columnDefinition = "text[]")
    private String[] attachments; // URLs to X-rays, images, etc.
    
    @Column(name = "category")
    private String category; // ROUTINE, EMERGENCY, CONSULTATION, FOLLOW_UP
    
    @Column(name = "is_draft")
    @Builder.Default
    private Boolean isDraft = false;
    
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1; // For version history
    
    @Column(name = "parent_note_id")
    private Long parentNoteId; // For note revisions
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    
    @Column(name = "signed_by")
    private Long signedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
