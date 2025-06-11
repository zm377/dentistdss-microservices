package com.dentistdss.clinicalrecords.model;

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
@Table(name = "dental_images")
public class DentalImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "dentist_id", nullable = false)
    private Long dentistId;
    
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;
    
    @Column(name = "clinical_note_id")
    private Long clinicalNoteId;
    
    @Column(name = "visit_id")
    private Long visitId;
    
    @Column(name = "gridfs_file_id", nullable = false)
    private String gridfsFileId; // MongoDB GridFS file ID
    
    @Column(name = "thumbnail_gridfs_id")
    private String thumbnailGridfsId; // Thumbnail file ID
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "image_type")
    private String imageType; // X_RAY, PHOTO, SCAN, OTHER
    
    @Column(name = "tooth_number")
    private String toothNumber;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String tags; // Comma-separated tags for search
    
    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false; // Primary image for the visit/note
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
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
