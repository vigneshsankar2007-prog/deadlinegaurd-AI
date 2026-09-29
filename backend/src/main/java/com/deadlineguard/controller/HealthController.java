package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check and application status endpoints for verification and monitoring.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private final Environment environment;
    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "deadlineguard-backend");
        health.put("timestamp", LocalDateTime.now());

        // Check database connection health
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", "CONNECTED");
            health.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
            health.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            health.put("database", "DISCONNECTED: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("DeadlineGuard API is running healthy", health));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("application", "DeadlineGuard AI");
        status.put("tagline", "Intelligent Student Task & Deadline Management System");
        status.put("version", "1.0.0-SNAPSHOT");
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("springBootVersion", "3.3.4");
        status.put("activeProfiles", environment.getActiveProfiles());
        status.put("stage", "STAGE 3 - Spring Boot Scaffolding & Configuration");
        status.put("entitiesConfigured", 8);
        status.put("repositoriesConfigured", 8);

        return ResponseEntity.ok(ApiResponse.success("DeadlineGuard system metadata", status));
    }
}
