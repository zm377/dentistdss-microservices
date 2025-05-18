package press.mizhifei.dentist.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import press.mizhifei.dentist.auth.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.DynamicUpdate;

/**
 * @author zhifeimi
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
    @SequenceGenerator(
        name = "user_sequence",
        sequenceName = "user_id_seq",
        allocationSize = 1,
        initialValue = 1000000000
    )
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL; // To track OAuth2 provider
    
    private String providerId; // User ID from the OAuth2 provider

    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiry;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    private String approvalRejectionReason;
    private String approvedBy;
    private LocalDateTime approvalDate;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    @Builder.Default
    private boolean accountNonLocked = true;

    private Long clinicId; // For Dentist, Receptionist, Clinic Admin
    private String clinicName; // For Clinic Admin


    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
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

    // Enum for Approval Status
    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public UserResponse toUserResponse() {
        return UserResponse.builder()
            .id(id.toString())
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .roles(roles)
            .clinicId(clinicId)
            .clinicName(clinicName)
            .build();
    }
}
