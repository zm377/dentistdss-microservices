package com.dentistdss.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "clinics")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clinic_id_seq")
    @SequenceGenerator(
        name = "clinic_id_seq",
        sequenceName = "clinic_id_seq",
        allocationSize = 1,
        initialValue = 100
    )
    private Long id;

    @Column(unique = true)
    private String name;

    // The admin of the clinic, each clinic has only one admin, stored in the relation table clinic_admin
    @OneToOne
    @JoinTable(
        name = "clinic_admin",
        joinColumns = @JoinColumn(name = "clinic_id"),
        inverseJoinColumns = @JoinColumn(name = "admin_id")
    )
    private User admin;

    private String address;
    private String city; //city or suburb
    private String state;
    private String zipCode;
    private String country;
    private String phoneNumber;
    private String email;
    private String website;

    private String taxId;

    private String licenseNumber;

    private LocalDate establishedDate;

    private String description;

    private String logoUrl;
    

    @Builder.Default
    private Boolean enabled = false;
    @Builder.Default
    private Boolean approved = false;
    // Reference to the SystemAdmin id who approved the clinic
    private Long approvalBy;

    private LocalDateTime approvalDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
