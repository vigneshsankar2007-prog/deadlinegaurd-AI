package com.deadlineguard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Google Gemini API integration.
 * Loaded from application.yml (gemini.*) and environment variables (GOOGLE_API_KEY, GEMINI_MODEL).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiConfig {

    /**
     * Gemini API Key provided via environment variable (GOOGLE_API_KEY).
     * Never hardcoded, never committed.
     */
    private String apiKey;

    /**
     * Gemini model name (default: gemini-2.5-flash).
     */
    private String model = "gemini-2.5-flash";

    /**
     * Request timeout in milliseconds (default: 10000ms = 10s).
     */
    private int timeoutMs = 10000;
}
