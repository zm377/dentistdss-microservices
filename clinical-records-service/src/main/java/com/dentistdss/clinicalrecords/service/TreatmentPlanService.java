package com.dentistdss.clinicalrecords.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dentistdss.clinicalrecords.client.AuthServiceClient;
import com.dentistdss.clinicalrecords.client.ClinicServiceClient;
import com.dentistdss.clinicalrecords.client.NotificationClient;
import com.dentistdss.clinicalrecords.dto.TreatmentPlanRequest;
import com.dentistdss.clinicalrecords.dto.TreatmentPlanResponse;
import com.dentistdss.clinicalrecords.model.TreatmentPlan;
import com.dentistdss.clinicalrecords.model.TreatmentPlanItem;
import com.dentistdss.clinicalrecords.repository.TreatmentPlanRepository;
import com.dentistdss.clinicalrecords.repository.TreatmentPlanItemRepository;

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
    private final AuthServiceClient authServiceClient;
    private final ClinicServiceClient clinicServiceClient;
    private final NotificationClient notificationClient;
    
    @Transactional
    public TreatmentPlanResponse createTreatmentPlan(TreatmentPlanRequest request) {
        // Handle plan versioning
        Integer version = 1;
        if (request.getParentPlanId() != null) {
            List<TreatmentPlan> versions = treatmentPlanRepository.findPlanVersions(request.getParentPlanId());
            version = versions.isEmpty() ? 2 : versions.get(0).getVersion() + 1;
        }
        
        TreatmentPlan treatmentPlan = TreatmentPlan.builder()
                .patientId(request.getPatientId())
                .dentistId(request.getDentistId())
                .clinicId(request.getClinicId())
                .planName(request.getPlanName())
                .description(request.getDescription())
                .totalCost(request.getTotalCost())
                .insuranceCoverage(request.getInsuranceCoverage())
                .patientCost(request.getPatientCost())
                .version(version)
                .parentPlanId(request.getParentPlanId())
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
        
        log.info("Created treatment plan {} for patient {} by dentist {}", 
                saved.getId(), saved.getPatientId(), saved.getDentistId());
        
        // Send notification to patient
        try {
            sendTreatmentPlanNotification(saved, "CREATED");
        } catch (Exception e) {
            log.warn("Failed to send treatment plan notification: {}", e.getMessage());
        }
        
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
        
        // Send notification
        try {
            sendTreatmentPlanNotification(saved, "ACCEPTED");
        } catch (Exception e) {
            log.warn("Failed to send treatment plan notification: {}", e.getMessage());
        }
        
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
        
        // Send notification
        try {
            sendTreatmentPlanNotification(saved, "COMPLETED");
        } catch (Exception e) {
            log.warn("Failed to send treatment plan notification: {}", e.getMessage());
        }
        
        return toResponse(saved);
    }
    
    @Transactional
    public TreatmentPlanResponse updateTreatmentPlanItemStatus(Integer planId, Integer itemId, String status) {
        TreatmentPlanItem item = treatmentPlanItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Treatment plan item not found"));
        
        if (!item.getTreatmentPlanId().equals(planId)) {
            throw new IllegalArgumentException("Item does not belong to the specified treatment plan");
        }
        
        item.setStatus(status);
        treatmentPlanItemRepository.save(item);
        
        log.info("Updated treatment plan item {} status to {}", itemId, status);
        
        // Check if all items are completed
        List<TreatmentPlanItem> allItems = treatmentPlanItemRepository
                .findByTreatmentPlanIdOrderBySequenceOrder(planId);
        boolean allCompleted = allItems.stream().allMatch(i -> "COMPLETED".equals(i.getStatus()));
        
        if (allCompleted) {
            TreatmentPlan plan = treatmentPlanRepository.findById(planId).orElse(null);
            if (plan != null && "IN_PROGRESS".equals(plan.getStatus())) {
                plan.setStatus("COMPLETED");
                plan.setCompletedAt(LocalDateTime.now());
                treatmentPlanRepository.save(plan);
                log.info("Auto-completed treatment plan {} as all items are completed", planId);
            }
        }
        
        return getTreatmentPlan(planId);
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
    
    @Transactional(readOnly = true)
    public List<TreatmentPlanResponse> getPlanVersions(Integer parentPlanId) {
        List<TreatmentPlan> plans = treatmentPlanRepository.findPlanVersions(parentPlanId);
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    private void sendTreatmentPlanNotification(TreatmentPlan plan, String action) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TREATMENT_PLAN_" + action);
        notification.put("recipientId", plan.getPatientId());
        notification.put("title", "Treatment Plan " + action);
        notification.put("message", "Your treatment plan '" + plan.getPlanName() + "' has been " + action.toLowerCase());
        notification.put("data", Map.of("treatmentPlanId", plan.getId()));
        
        notificationClient.sendNotification(notification);
    }
    
    private TreatmentPlanResponse toResponse(TreatmentPlan treatmentPlan) {
        // Get treatment plan items
        List<TreatmentPlanItem> items = treatmentPlanItemRepository
                .findByTreatmentPlanIdOrderBySequenceOrder(treatmentPlan.getId());
        
        List<TreatmentPlanResponse.TreatmentPlanItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        
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
                .version(treatmentPlan.getVersion())
                .parentPlanId(treatmentPlan.getParentPlanId())
                .createdAt(treatmentPlan.getCreatedAt())
                .acceptedAt(treatmentPlan.getAcceptedAt())
                .completedAt(treatmentPlan.getCompletedAt())
                .items(itemResponses)
                .build();
        
        // Fetch names from services
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
        
        try {
            response.setClinicName(clinicServiceClient.getClinic(treatmentPlan.getClinicId()).getName());
        } catch (Exception e) {
            log.warn("Failed to fetch clinic name for id {}: {}", treatmentPlan.getClinicId(), e.getMessage());
        }
        
        return response;
    }
    
    private TreatmentPlanResponse.TreatmentPlanItemResponse toItemResponse(TreatmentPlanItem item) {
        TreatmentPlanResponse.TreatmentPlanItemResponse response = TreatmentPlanResponse.TreatmentPlanItemResponse.builder()
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
            try {
                response.setServiceName(clinicServiceClient.getService(item.getServiceId()).getName());
            } catch (Exception e) {
                log.warn("Failed to fetch service name for id {}: {}", item.getServiceId(), e.getMessage());
            }
        }
        
        return response;
    }
}
