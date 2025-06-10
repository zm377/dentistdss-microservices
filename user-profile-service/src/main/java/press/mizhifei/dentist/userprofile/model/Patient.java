package press.mizhifei.dentist.userprofile.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
@Table(name = "patients")
public class Patient {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // is the same as the user id
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String address;

    /**
     * Free-form JSON or text that captures health history. For PoC this is just a String.
     */
    @Column(columnDefinition = "TEXT")
    private String healthHistory;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 