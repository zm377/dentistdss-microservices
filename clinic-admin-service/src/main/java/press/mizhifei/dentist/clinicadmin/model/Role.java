package press.mizhifei.dentist.clinicadmin.model;

/**
 * Role enum for clinic administration service
 * 
 * Defines user roles with specific access levels for clinic administration
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
public enum Role {
    SYSTEM_ADMIN("System Administrator"),
    CLINIC_ADMIN("Clinic Administrator"), 
    DENTIST("Dentist"),
    RECEPTIONIST("Receptionist"),
    PATIENT("Patient");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        try {
            return Role.valueOf(role.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Check if this role has clinic administration privileges
     */
    public boolean hasClinicAdminPrivileges() {
        return this == SYSTEM_ADMIN || this == CLINIC_ADMIN;
    }

    /**
     * Check if this role has clinic staff privileges
     */
    public boolean hasClinicStaffPrivileges() {
        return this == SYSTEM_ADMIN || this == CLINIC_ADMIN || this == RECEPTIONIST || this == DENTIST;
    }

    /**
     * Check if this role can view clinic information
     */
    public boolean canViewClinicInfo() {
        return this != PATIENT; // All roles except patient can view clinic info
    }
}
