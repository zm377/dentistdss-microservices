package press.mizhifei.dentist.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
@Table(name = "user_approval_requests")
public class UserApprovalRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "requested_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role requestedRole;
    
    @Column(name = "clinic_id")
    private Long clinicId;
    
    @Column(name = "status")
    @Convert(converter = PostgreSQLEnumType.class)
    @Builder.Default
    private User.ApprovalStatus status = User.ApprovalStatus.PENDING;
    
    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;
    
    @Column(name = "supporting_documents", columnDefinition = "text[]")
    private String[] supportingDocuments;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 