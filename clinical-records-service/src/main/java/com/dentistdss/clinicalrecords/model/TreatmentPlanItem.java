package com.dentistdss.clinicalrecords.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "treatment_plan_items")
public class TreatmentPlanItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "treatment_plan_id", nullable = false)
    private Integer treatmentPlanId;
    
    @Column(name = "service_id")
    private Integer serviceId;
    
    @Column(name = "tooth_number", length = 10)
    private String toothNumber;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;
    
    @Column(length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SCHEDULED, COMPLETED
    
    @Column(name = "sequence_order")
    private Integer sequenceOrder;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // Many-to-One relationship with TreatmentPlan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_plan_id", insertable = false, updatable = false)
    private TreatmentPlan treatmentPlan;
}
