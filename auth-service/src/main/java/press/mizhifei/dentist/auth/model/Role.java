package press.mizhifei.dentist.auth.model;


/**
 * @author zhifeimi
 *
 * Defines the roles available in the system.
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
