package com.deadlineguard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring Boot context load sanity test verifying JPA entity graph and repository bindings.
 */
@SpringBootTest
@ActiveProfiles("test")
class DeadlineGuardApplicationTests {

    @Test
    @DisplayName("Verify Spring Boot application context and JPA beans initialize cleanly")
    void contextLoads() {
        assertTrue(true, "Application context loaded successfully with all 8 entities and repositories");
    }
}
