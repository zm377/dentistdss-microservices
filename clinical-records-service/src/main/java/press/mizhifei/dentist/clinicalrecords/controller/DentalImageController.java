package press.mizhifei.dentist.clinicalrecords.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import press.mizhifei.dentist.clinicalrecords.dto.ApiResponse;
import press.mizhifei.dentist.clinicalrecords.dto.DentalImageResponse;
import press.mizhifei.dentist.clinicalrecords.service.DentalImageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@RestController
@RequestMapping("/clinical-records/image")
@RequiredArgsConstructor
public class DentalImageController {
    
    private final DentalImageService dentalImageService;
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DentalImageResponse>> uploadDentalImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") Long patientId,
            @RequestParam("dentistId") Long dentistId,
            @RequestParam("clinicId") Long clinicId,
            @RequestParam(value = "clinicalNoteId", required = false) Long clinicalNoteId,
            @RequestParam(value = "visitId", required = false) Long visitId,
            @RequestParam("imageType") String imageType,
            @RequestParam(value = "toothNumber", required = false) String toothNumber,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {
        
        try {
            DentalImageResponse response = dentalImageService.uploadDentalImage(
                    file, patientId, dentistId, clinicId, clinicalNoteId, visitId,
                    imageType, toothNumber, description, tags);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadDentalImage(@PathVariable Long id) {
        try {
            InputStream imageStream = dentalImageService.downloadDentalImage(id);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dental_image_" + id + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(imageStream));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<InputStreamResource> downloadThumbnail(@PathVariable Long id) {
        try {
            InputStream thumbnailStream = dentalImageService.downloadThumbnail(id);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(thumbnailStream));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDentalImage(@PathVariable Long id) {
        try {
            dentalImageService.deleteDentalImage(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/metadata")
    public ResponseEntity<ApiResponse<DentalImageResponse>> updateImageMetadata(
            @PathVariable Long id,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "toothNumber", required = false) String toothNumber,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        
        try {
            DentalImageResponse response = dentalImageService.updateImageMetadata(
                    id, description, tags, toothNumber, isPrimary);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> getPatientImages(
            @PathVariable Long patientId) {
        List<DentalImageResponse> images = dentalImageService.getPatientImages(patientId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    @GetMapping("/patient/{patientId}/type/{imageType}")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> getPatientImagesByType(
            @PathVariable Long patientId,
            @PathVariable String imageType) {
        List<DentalImageResponse> images = dentalImageService.getPatientImagesByType(patientId, imageType);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    @GetMapping("/patient/{patientId}/tooth/{toothNumber}")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> getPatientImagesByTooth(
            @PathVariable Long patientId,
            @PathVariable String toothNumber) {
        List<DentalImageResponse> images = dentalImageService.getPatientImagesByTooth(patientId, toothNumber);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    @GetMapping("/patient/{patientId}/search")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> searchPatientImagesByTag(
            @PathVariable Long patientId,
            @RequestParam String tag) {
        List<DentalImageResponse> images = dentalImageService.searchPatientImagesByTag(patientId, tag);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    @GetMapping("/note/{clinicalNoteId}")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> getClinicalNoteImages(
            @PathVariable Long clinicalNoteId) {
        List<DentalImageResponse> images = dentalImageService.getClinicalNoteImages(clinicalNoteId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
    
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<ApiResponse<List<DentalImageResponse>>> getVisitImages(
            @PathVariable Long visitId) {
        List<DentalImageResponse> images = dentalImageService.getVisitImages(visitId);
        return ResponseEntity.ok(ApiResponse.success(images));
    }
}
