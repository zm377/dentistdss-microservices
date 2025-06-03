package press.mizhifei.dentist.clinic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "treatment_plans")
public class TreatmentPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "dentist_id", nullable = false)
    private Long dentistId;
    
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;
    
    @Column(name = "plan_name")
    private String planName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "insurance_coverage", precision = 10, scale = 2)
    private BigDecimal insuranceCoverage;
    
    @Column(name = "patient_cost", precision = 10, scale = 2)
    private BigDecimal patientCost;
    
    @Column(length = 50)
    @Builder.Default
    private String status = "PROPOSED"; // PROPOSED, ACCEPTED, IN_PROGRESS, COMPLETED
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // One-to-Many relationship with TreatmentPlanItem
    @OneToMany(mappedBy = "treatmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TreatmentPlanItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public void addItem(TreatmentPlanItem item) {
        items.add(item);
        item.setTreatmentPlan(this);
        item.setTreatmentPlanId(this.id);
    }
    
    public void removeItem(TreatmentPlanItem item) {
        items.remove(item);
        item.setTreatmentPlan(null);
        item.setTreatmentPlanId(null);
    }
} 