package com.dentistdss.clinicalrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicalrecords.model.DentalImage;

import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface DentalImageRepository extends JpaRepository<DentalImage, Long> {
    
    List<DentalImage> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    List<DentalImage> findByClinicalNoteIdOrderByCreatedAtDesc(Long clinicalNoteId);
    
    List<DentalImage> findByVisitIdOrderByCreatedAtDesc(Long visitId);
    
    List<DentalImage> findByPatientIdAndImageType(Long patientId, String imageType);
    
    @Query("SELECT di FROM DentalImage di WHERE di.patientId = :patientId " +
           "AND di.toothNumber = :toothNumber ORDER BY di.createdAt DESC")
    List<DentalImage> findByPatientIdAndToothNumber(@Param("patientId") Long patientId,
                                                   @Param("toothNumber") String toothNumber);
    
    @Query("SELECT di FROM DentalImage di WHERE di.patientId = :patientId " +
           "AND di.tags ILIKE %:tag% ORDER BY di.createdAt DESC")
    List<DentalImage> findByPatientIdAndTag(@Param("patientId") Long patientId,
                                           @Param("tag") String tag);
}
