package com.dentistdss.clinicalrecords.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.dentistdss.clinicalrecords.client.AuthServiceClient;
import com.dentistdss.clinicalrecords.client.ClinicServiceClient;
import com.dentistdss.clinicalrecords.config.FileUploadConfig;
import com.dentistdss.clinicalrecords.dto.DentalImageResponse;
import com.dentistdss.clinicalrecords.model.DentalImage;
import com.dentistdss.clinicalrecords.repository.DentalImageRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
public class DentalImageService {
    
    private final DentalImageRepository dentalImageRepository;
    private final AuthServiceClient authServiceClient;
    private final ClinicServiceClient clinicServiceClient;
    private final GridFSBucket gridFSBucket;
    @Qualifier("thumbnailGridFSBucket")
    private final GridFSBucket thumbnailGridFSBucket;
    private final FileUploadConfig fileUploadConfig;
    
    @Transactional
    public DentalImageResponse uploadDentalImage(MultipartFile file, 
                                               Long patientId, 
                                               Long dentistId, 
                                               Long clinicId,
                                               Long clinicalNoteId,
                                               Long visitId,
                                               String imageType,
                                               String toothNumber,
                                               String description,
                                               String tags) throws IOException {
        
        // Validate file
        validateImageFile(file);
        
        // Upload original image to GridFS
        Document metadata = new Document()
                .append("patientId", patientId)
                .append("dentistId", dentistId)
                .append("clinicId", clinicId)
                .append("imageType", imageType)
                .append("contentType", file.getContentType());

        GridFSUploadOptions uploadOptions = new GridFSUploadOptions().metadata(metadata);
        ObjectId fileId = gridFSBucket.uploadFromStream(
                file.getOriginalFilename(),
                file.getInputStream(),
                uploadOptions
        );
        
        // Generate and upload thumbnail
        String thumbnailFileId = null;
        try {
            byte[] thumbnailBytes = generateThumbnail(file);
            if (thumbnailBytes != null) {
                Document thumbnailMetadata = new Document()
                        .append("originalFileId", fileId.toString())
                        .append("patientId", patientId)
                        .append("imageType", "THUMBNAIL");

                GridFSUploadOptions thumbnailUploadOptions = new GridFSUploadOptions().metadata(thumbnailMetadata);
                ObjectId thumbnailId = thumbnailGridFSBucket.uploadFromStream(
                        "thumb_" + file.getOriginalFilename(),
                        new ByteArrayInputStream(thumbnailBytes),
                        thumbnailUploadOptions
                );
                thumbnailFileId = thumbnailId.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to generate thumbnail for image {}: {}", file.getOriginalFilename(), e.getMessage());
        }
        
        // Save metadata to PostgreSQL
        DentalImage dentalImage = DentalImage.builder()
                .patientId(patientId)
                .dentistId(dentistId)
                .clinicId(clinicId)
                .clinicalNoteId(clinicalNoteId)
                .visitId(visitId)
                .gridfsFileId(fileId.toString())
                .thumbnailGridfsId(thumbnailFileId)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .imageType(imageType)
                .toothNumber(toothNumber)
                .description(description)
                .tags(tags)
                .build();
        
        DentalImage saved = dentalImageRepository.save(dentalImage);
        log.info("Uploaded dental image {} for patient {} by dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public InputStream downloadDentalImage(Long imageId) {
        DentalImage dentalImage = dentalImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Dental image not found"));
        
        ObjectId fileId = new ObjectId(dentalImage.getGridfsFileId());
        return gridFSBucket.openDownloadStream(fileId);
    }
    
    @Transactional(readOnly = true)
    public InputStream downloadThumbnail(Long imageId) {
        DentalImage dentalImage = dentalImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Dental image not found"));
        
        if (dentalImage.getThumbnailGridfsId() == null) {
            throw new IllegalArgumentException("Thumbnail not available for this image");
        }
        
        ObjectId thumbnailId = new ObjectId(dentalImage.getThumbnailGridfsId());
        return thumbnailGridFSBucket.openDownloadStream(thumbnailId);
    }
    
    @Transactional
    public void deleteDentalImage(Long imageId) {
        DentalImage dentalImage = dentalImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Dental image not found"));
        
        // Delete from GridFS
        ObjectId fileId = new ObjectId(dentalImage.getGridfsFileId());
        gridFSBucket.delete(fileId);
        
        // Delete thumbnail if exists
        if (dentalImage.getThumbnailGridfsId() != null) {
            ObjectId thumbnailId = new ObjectId(dentalImage.getThumbnailGridfsId());
            thumbnailGridFSBucket.delete(thumbnailId);
        }
        
        // Delete metadata from PostgreSQL
        dentalImageRepository.delete(dentalImage);
        
        log.info("Deleted dental image {} and associated files", imageId);
    }
    
    @Transactional
    public DentalImageResponse updateImageMetadata(Long imageId, 
                                                 String description, 
                                                 String tags, 
                                                 String toothNumber,
                                                 Boolean isPrimary) {
        DentalImage dentalImage = dentalImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Dental image not found"));
        
        if (description != null) dentalImage.setDescription(description);
        if (tags != null) dentalImage.setTags(tags);
        if (toothNumber != null) dentalImage.setToothNumber(toothNumber);
        if (isPrimary != null) dentalImage.setIsPrimary(isPrimary);
        
        DentalImage saved = dentalImageRepository.save(dentalImage);
        log.info("Updated metadata for dental image {}", imageId);
        
        return toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> getPatientImages(Long patientId) {
        List<DentalImage> images = dentalImageRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> getClinicalNoteImages(Long clinicalNoteId) {
        List<DentalImage> images = dentalImageRepository.findByClinicalNoteIdOrderByCreatedAtDesc(clinicalNoteId);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> getVisitImages(Long visitId) {
        List<DentalImage> images = dentalImageRepository.findByVisitIdOrderByCreatedAtDesc(visitId);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> getPatientImagesByType(Long patientId, String imageType) {
        List<DentalImage> images = dentalImageRepository.findByPatientIdAndImageType(patientId, imageType);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> getPatientImagesByTooth(Long patientId, String toothNumber) {
        List<DentalImage> images = dentalImageRepository.findByPatientIdAndToothNumber(patientId, toothNumber);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DentalImageResponse> searchPatientImagesByTag(Long patientId, String tag) {
        List<DentalImage> images = dentalImageRepository.findByPatientIdAndTag(patientId, tag);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(fileUploadConfig.getAllowedImageTypes()).contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed");
        }
    }
    
    private byte[] generateThumbnail(MultipartFile file) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .size(fileUploadConfig.getThumbnailWidth(), fileUploadConfig.getThumbnailHeight())
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);
            
            return outputStream.toByteArray();
        }
    }
    
    private DentalImageResponse toResponse(DentalImage dentalImage) {
        DentalImageResponse response = DentalImageResponse.builder()
                .id(dentalImage.getId())
                .patientId(dentalImage.getPatientId())
                .dentistId(dentalImage.getDentistId())
                .clinicId(dentalImage.getClinicId())
                .clinicalNoteId(dentalImage.getClinicalNoteId())
                .visitId(dentalImage.getVisitId())
                .gridfsFileId(dentalImage.getGridfsFileId())
                .thumbnailGridfsId(dentalImage.getThumbnailGridfsId())
                .originalFilename(dentalImage.getOriginalFilename())
                .contentType(dentalImage.getContentType())
                .fileSize(dentalImage.getFileSize())
                .imageType(dentalImage.getImageType())
                .toothNumber(dentalImage.getToothNumber())
                .description(dentalImage.getDescription())
                .tags(dentalImage.getTags())
                .isPrimary(dentalImage.getIsPrimary())
                .createdAt(dentalImage.getCreatedAt())
                .updatedAt(dentalImage.getUpdatedAt())
                .downloadUrl("/clinical-records/image/" + dentalImage.getId() + "/download")
                .thumbnailUrl(dentalImage.getThumbnailGridfsId() != null ? 
                        "/clinical-records/image/" + dentalImage.getId() + "/thumbnail" : null)
                .build();
        
        // Fetch names from services
        try {
            response.setPatientName(authServiceClient.getUserFullName(dentalImage.getPatientId()));
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for id {}: {}", dentalImage.getPatientId(), e.getMessage());
        }
        
        try {
            response.setDentistName(authServiceClient.getUserFullName(dentalImage.getDentistId()));
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for id {}: {}", dentalImage.getDentistId(), e.getMessage());
        }
        
        try {
            response.setClinicName(clinicServiceClient.getClinic(dentalImage.getClinicId()).getName());
        } catch (Exception e) {
            log.warn("Failed to fetch clinic name for id {}: {}", dentalImage.getClinicId(), e.getMessage());
        }
        
        return response;
    }
}
