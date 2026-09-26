package com.deadlineguard.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Basic health endpoint tests verifying unauthenticated infrastructure endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/health returns HTTP 200 with service UP status")
    void getHealthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("deadlineguard-backend"));
    }

    @Test
    @DisplayName("GET /api/v1/status returns HTTP 200 with system metadata")
    void getStatusReturnsMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.application").value("DeadlineGuard AI"))
                .andExpect(jsonPath("$.data.stage").value("STAGE 3 - Spring Boot Scaffolding & Configuration"))
                .andExpect(jsonPath("$.data.entitiesConfigured").value(8))
                .andExpect(jsonPath("$.data.repositoriesConfigured").value(8));
    }
}
