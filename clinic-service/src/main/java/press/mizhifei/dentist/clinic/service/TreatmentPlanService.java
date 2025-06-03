package press.mizhifei.dentist.clinic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import press.mizhifei.dentist.clinic.client.AuthServiceClient;
import press.mizhifei.dentist.clinic.client.NotificationClient;
import press.mizhifei.dentist.clinic.dto.TreatmentPlanRequest;
import press.mizhifei.dentist.clinic.dto.TreatmentPlanResponse;
import press.mizhifei.dentist.clinic.model.TreatmentPlan;
import press.mizhifei.dentist.clinic.model.TreatmentPlanItem;
import press.mizhifei.dentist.clinic.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class TreatmentPlanService {
    
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final TreatmentPlanItemRepository treatmentPlanItemRepository;
    private final ServiceRepository serviceRepository;
    private final ClinicRepository clinicRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationClient notificationClient;
    
    @Transactional
    public TreatmentPlanResponse createTreatmentPlan(TreatmentPlanRequest request) {
        TreatmentPlan treatmentPlan = TreatmentPlan.builder()
                .patientId(request.getPatientId())
                .dentistId(request.getDentistId())
                .clinicId(request.getClinicId())
                .planName(request.getPlanName())
                .description(request.getDescription())
                .totalCost(request.getTotalCost())
                .insuranceCoverage(request.getInsuranceCoverage())
                .patientCost(request.getPatientCost())
                .status("PROPOSED")
                .build();
        
        TreatmentPlan saved = treatmentPlanRepository.save(treatmentPlan);
        
        // Create treatment plan items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int sequenceOrder = 1;
            for (TreatmentPlanRequest.TreatmentPlanItemRequest itemRequest : request.getItems()) {
                TreatmentPlanItem item = TreatmentPlanItem.builder()
                        .treatmentPlanId(saved.getId())
                        .serviceId(itemRequest.getServiceId())
                        .toothNumber(itemRequest.getToothNumber())
                        .description(itemRequest.getDescription())
                        .cost(itemRequest.getCost())
                        .sequenceOrder(itemRequest.getSequenceOrder() != null ? 
                                itemRequest.getSequenceOrder() : sequenceOrder++)
                        .notes(itemRequest.getNotes())
                        .status("PENDING")
                        .build();
                
                treatmentPlanItemRepository.save(item);
            }
        }
        
        // Calculate total cost if not provided
        if (request.getTotalCost() == null) {
            calculateAndUpdateCosts(saved);
        }
        
        log.info("Created treatment plan {} for patient {} by dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        // Send notification to patient
        sendTreatmentPlanNotification(saved);
        
        return toResponse(saved);
    }
    
    @Transactional
    public TreatmentPlanResponse acceptTreatmentPlan(Integer planId) {
        TreatmentPlan treatmentPlan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan not found"));
        
        if (!"PROPOSED".equals(treatmentPlan.getStatus())) {
            throw new IllegalStateException("Only proposed treatment plans can be accepted");
        }
        
        treatmentPlan.setStatus("ACCEPTED");
        treatmentPlan.setAcceptedAt(LocalDateTime.now());
        
        TreatmentPlan saved = treatmentPlanRepository.save(treatmentPlan);
        log.info("Accepted treatment plan {}", planId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public TreatmentPlanResponse startTreatmentPlan(Integer planId) {
        TreatmentPlan treatmentPlan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan not found"));
        
        if (!"ACCEPTED".equals(treatmentPlan.getStatus())) {
            throw new IllegalStateException("Only accepted treatment plans can be started");
        }
        
        treatmentPlan.setStatus("IN_PROGRESS");
        
        TreatmentPlan saved = treatmentPlanRepository.save(treatmentPlan);
        log.info("Started treatment plan {}", planId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public TreatmentPlanResponse completeTreatmentPlan(Integer planId) {
        TreatmentPlan treatmentPlan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan not found"));
        
        if (!"IN_PROGRESS".equals(treatmentPlan.getStatus())) {
            throw new IllegalStateException("Only in-progress treatment plans can be completed");
        }
        
        treatmentPlan.setStatus("COMPLETED");
        treatmentPlan.setCompletedAt(LocalDateTime.now());
        
        TreatmentPlan saved = treatmentPlanRepository.save(treatmentPlan);
        log.info("Completed treatment plan {}", planId);
        
        return toResponse(saved);
    }
    
    @Transactional
    public TreatmentPlanResponse updateTreatmentPlanItemStatus(Integer planId, Integer itemId, String status) {
        TreatmentPlan treatmentPlan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan not found"));
        
        TreatmentPlanItem item = treatmentPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan item not found"));
        
        if (!item.getTreatmentPlanId().equals(planId)) {
            throw new IllegalArgumentException("Item does not belong to this treatment plan");
        }
        
        item.setStatus(status);
        treatmentPlanItemRepository.save(item);
        
        log.info("Updated treatment plan item {} status to {}", itemId, status);
        
        return toResponse(treatmentPlan);
    }
    
    @Transactional(readOnly = true)
    public TreatmentPlanResponse getTreatmentPlan(Integer planId) {
        TreatmentPlan treatmentPlan = treatmentPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan not found"));
        
        return toResponse(treatmentPlan);
    }
    
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getPatientTreatmentPlans(Long patientId) {
        List<TreatmentPlan> plans = treatmentPlanRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getDentistTreatmentPlans(Long dentistId) {
        List<TreatmentPlan> plans = treatmentPlanRepository.findByDentistIdOrderByCreatedAtDesc(dentistId);
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    private void calculateAndUpdateCosts(TreatmentPlan treatmentPlan) {
        List<TreatmentPlanItem> items = treatmentPlanItemRepository
                .findByTreatmentPlanIdOrderBySequenceOrder(treatmentPlan.getId());
        
        BigDecimal totalCost = items.stream()
                .map(TreatmentPlanItem::getCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        treatmentPlan.setTotalCost(totalCost);
        
        if (treatmentPlan.getInsuranceCoverage() != null) {
            treatmentPlan.setPatientCost(totalCost.subtract(treatmentPlan.getInsuranceCoverage()));
        } else {
            treatmentPlan.setPatientCost(totalCost);
        }
        
        treatmentPlanRepository.save(treatmentPlan);
    }
    
    private void sendTreatmentPlanNotification(TreatmentPlan treatmentPlan) {
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", treatmentPlan.getPatientId());
            notificationRequest.put("templateName", "treatment_plan_ready");
            notificationRequest.put("type", "EMAIL");
            
            Map<String, String> templateVariables = new HashMap<>();
            
            try {
                String patientName = authServiceClient.getUserFullName(treatmentPlan.getPatientId());
                templateVariables.put("patient_name", patientName);
            } catch (Exception e) {
                log.warn("Failed to fetch patient name for notification: {}", e.getMessage());
                templateVariables.put("patient_name", "Patient");
            }
            
            try {
                String dentistName = authServiceClient.getUserFullName(treatmentPlan.getDentistId());
                templateVariables.put("dentist_name", dentistName);
            } catch (Exception e) {
                log.warn("Failed to fetch dentist name for notification: {}", e.getMessage());
                templateVariables.put("dentist_name", "Dr. Dentist");
            }
            
            notificationRequest.put("templateVariables", templateVariables);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("treatment_plan_id", treatmentPlan.getId());
            notificationRequest.put("metadata", metadata);
            
            notificationClient.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send treatment plan notification: {}", e.getMessage());
        }
    }
    
    private TreatmentPlanResponse toResponse(TreatmentPlan treatmentPlan) {
        TreatmentPlanResponse response = TreatmentPlanResponse.builder()
                .id(treatmentPlan.getId())
                .patientId(treatmentPlan.getPatientId())
                .dentistId(treatmentPlan.getDentistId())
                .clinicId(treatmentPlan.getClinicId())
                .planName(treatmentPlan.getPlanName())
                .description(treatmentPlan.getDescription())
                .totalCost(treatmentPlan.getTotalCost())
                .insuranceCoverage(treatmentPlan.getInsuranceCoverage())
                .patientCost(treatmentPlan.getPatientCost())
                .status(treatmentPlan.getStatus())
                .createdAt(treatmentPlan.getCreatedAt())
                .acceptedAt(treatmentPlan.getAcceptedAt())
                .completedAt(treatmentPlan.getCompletedAt())
                .build();
        
        // Fetch names from user service
        try {
            response.setPatientName(authServiceClient.getUserFullName(treatmentPlan.getPatientId()));
        } catch (Exception e) {
            log.warn("Failed to fetch patient name for id {}: {}", treatmentPlan.getPatientId(), e.getMessage());
        }
        
        try {
            response.setDentistName(authServiceClient.getUserFullName(treatmentPlan.getDentistId()));
        } catch (Exception e) {
            log.warn("Failed to fetch dentist name for id {}: {}", treatmentPlan.getDentistId(), e.getMessage());
        }
        
        // Fetch clinic name
        clinicRepository.findById(treatmentPlan.getClinicId()).ifPresent(clinic -> 
                response.setClinicName(clinic.getName())
        );
        
        // Fetch items
        List<TreatmentPlanItem> items = treatmentPlanItemRepository
                .findByTreatmentPlanIdOrderBySequenceOrder(treatmentPlan.getId());
        
        List<TreatmentPlanResponse.TreatmentPlanItemResponse> itemResponses = items.stream()
                .map(item -> {
                    TreatmentPlanResponse.TreatmentPlanItemResponse itemResponse = 
                            TreatmentPlanResponse.TreatmentPlanItemResponse.builder()
                                    .id(item.getId())
                                    .serviceId(item.getServiceId())
                                    .toothNumber(item.getToothNumber())
                                    .description(item.getDescription())
                                    .cost(item.getCost())
                                    .status(item.getStatus())
                                    .sequenceOrder(item.getSequenceOrder())
                                    .notes(item.getNotes())
                                    .build();
                    
                    // Fetch service name
                    if (item.getServiceId() != null) {
                        serviceRepository.findById(item.getServiceId()).ifPresent(service -> 
                                itemResponse.setServiceName(service.getName())
                        );
                    }
                    
                    return itemResponse;
                })
                .collect(Collectors.toList());
        
        response.setItems(itemResponses);
        
        return response;
    }
} 