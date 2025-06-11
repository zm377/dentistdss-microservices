package com.dentistdss.clinicadmin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Basic test for Clinic Admin Service Application
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
class ClinicAdminServiceApplicationTest {

    @Test
    void applicationMainMethodExists() {
        // This test verifies that the main application class exists and has a main method
        assertNotNull(ClinicAdminServiceApplication.class);

        try {
            ClinicAdminServiceApplication.class.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method not found", e);
        }
    }
}
