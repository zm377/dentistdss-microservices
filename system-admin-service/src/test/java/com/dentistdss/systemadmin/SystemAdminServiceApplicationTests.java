package com.dentistdss.systemadmin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for System Admin Service Application
 *
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@SpringBootTest
@ActiveProfiles("test")
class SystemAdminServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }

    @Test
    void applicationStarts() {
        // Test that the application starts without errors
        // This test will pass if the Spring Boot application context loads successfully
    }
}
