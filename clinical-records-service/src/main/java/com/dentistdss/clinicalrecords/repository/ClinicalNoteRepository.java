package com.dentistdss.clinicalrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dentistdss.clinicalrecords.model.ClinicalNote;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {

    List<ClinicalNote> findByClinicIdOrderByCreatedAtDesc(Long clinicId);
    
    List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    List<ClinicalNote> findByDentistIdOrderByCreatedAtDesc(Long dentistId);
    
    List<ClinicalNote> findByVisitIdOrderByCreatedAtDesc(Long visitId);
    
    Optional<ClinicalNote> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.patientId = :patientId " +
           "AND cn.isDraft = false ORDER BY cn.createdAt DESC")
    List<ClinicalNote> findSignedNotesByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.dentistId = :dentistId " +
           "AND cn.isDraft = true ORDER BY cn.updatedAt DESC")
    List<ClinicalNote> findDraftNotesByDentistId(@Param("dentistId") Long dentistId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.patientId = :patientId " +
           "AND cn.category = :category ORDER BY cn.createdAt DESC")
    List<ClinicalNote> findByPatientIdAndCategory(@Param("patientId") Long patientId, 
                                                  @Param("category") String category);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.parentNoteId = :parentNoteId " +
           "ORDER BY cn.version DESC")
    List<ClinicalNote> findNoteVersions(@Param("parentNoteId") Long parentNoteId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.patientId = :patientId " +
           "AND (cn.chiefComplaint ILIKE %:searchTerm% " +
           "OR cn.diagnosis ILIKE %:searchTerm% " +
           "OR cn.treatmentPerformed ILIKE %:searchTerm%) " +
           "ORDER BY cn.createdAt DESC")
    List<ClinicalNote> searchNotesByPatient(@Param("patientId") Long patientId, 
                                           @Param("searchTerm") String searchTerm);
}
