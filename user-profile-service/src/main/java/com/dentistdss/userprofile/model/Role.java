package com.dentistdss.userprofile.model;

/**
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 *
 */

public enum Role {
    SYSTEM_ADMIN,
    CLINIC_ADMIN, // Dental Clinic Administrator
    DENTIST,
    RECEPTIONIST,
    PATIENT;

    public static Role fromString(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
