package press.mizhifei.dentist.clinic.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import press.mizhifei.dentist.clinic.dto.PatientResponse;

import java.util.List;

/**
 * Feign client for patient-service
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@FeignClient(name = "patient-service", path = "/patient")
public interface PatientServiceClient {
    
    @GetMapping("/list/all")
    List<PatientResponse> getAllPatients();
    
    @GetMapping("/{id}")
    PatientResponse getPatientById(@PathVariable("id") Long patientId);
}
