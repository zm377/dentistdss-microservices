package press.mizhifei.dentist.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import press.mizhifei.dentist.clinic.model.DentistAvailability;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Repository
public interface DentistAvailabilityRepository extends JpaRepository<DentistAvailability, Integer> {
    
    List<DentistAvailability> findByDentistIdAndClinicIdAndAvailableDateBetween(
            Long dentistId, Long clinicId, LocalDate startDate, LocalDate endDate);
    
    List<DentistAvailability> findByDentistIdAndAvailableDate(Long dentistId, LocalDate date);
    
    @Query("SELECT da FROM DentistAvailability da WHERE da.dentistId = :dentistId " +
           "AND da.clinicId = :clinicId " +
           "AND da.availableDate = :date " +
           "AND da.isBlocked = false " +
           "ORDER BY da.startTime")
    List<DentistAvailability> findAvailableSlots(@Param("dentistId") Long dentistId,
                                                  @Param("clinicId") Long clinicId,
                                                  @Param("date") LocalDate date);

    List<DentistAvailability> findByDentistIdAndAvailableDateBetween(Long dentistId, LocalDate startDate, LocalDate endDate);
}