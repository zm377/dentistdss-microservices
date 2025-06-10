package press.mizhifei.dentist.clinicalrecords.dto;

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
public class DentalImageResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long dentistId;
    private String dentistName;
    private Long clinicId;
    private String clinicName;
    private Long clinicalNoteId;
    private Long visitId;
    private String gridfsFileId;
    private String thumbnailGridfsId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String imageType;
    private String toothNumber;
    private String description;
    private String tags;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
    private String thumbnailUrl;
}
