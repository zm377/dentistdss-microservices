package press.mizhifei.dentist.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinic.model.ClinicalNote;

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
    
    Optional<ClinicalNote> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.patientId = :patientId " +
           "AND cn.isDraft = false ORDER BY cn.createdAt DESC")
    List<ClinicalNote> findSignedNotesByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT cn FROM ClinicalNote cn WHERE cn.dentistId = :dentistId " +
           "AND cn.isDraft = true ORDER BY cn.updatedAt DESC")
    List<ClinicalNote> findDraftNotesByDentistId(@Param("dentistId") Long dentistId);
} 