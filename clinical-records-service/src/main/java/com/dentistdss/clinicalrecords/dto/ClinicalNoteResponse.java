package com.dentistdss.clinicalrecords.dto;

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
@AllArgsConstructor
@NoArgsConstructor
public class ClinicalNoteResponse {
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private Long clinicId;
    private String clinicName;
    private Long visitId;
    private String chiefComplaint;
    private String examinationFindings;
    private String diagnosis;
    private String treatmentPerformed;
    private String treatmentPlan;
    private String prescriptions;
    private String followUpInstructions;
    private String aiAssistedNotes;
    private String[] attachments;
    private String category;
    private Boolean isDraft;
    private Integer version;
    private Long parentNoteId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime signedAt;
    private Long signedBy;
    private String signedByName;
}
