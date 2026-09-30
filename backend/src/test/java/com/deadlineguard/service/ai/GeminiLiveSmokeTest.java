package com.deadlineguard.service.ai;

import com.deadlineguard.config.GeminiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optional Live Gemini Smoke Test.
 *
 * This test is STRICTLY OPTIONAL and disabled by default.
 * It will ONLY execute when a live, non-empty GOOGLE_API_KEY environment variable is explicitly provided.
 * Automated test suites and CI runs will safely skip this test.
 */
class GeminiLiveSmokeTest {

    @Test
    @DisplayName("Manual Smoke Test: Connects to live Google Gemini API when GOOGLE_API_KEY is present")
    @EnabledIfEnvironmentVariable(named = "GOOGLE_API_KEY", matches = "^[a-zA-Z0-9_-]{10,}$")
    void liveGeminiCall_succeedsWhenApiKeyProvided() throws Exception {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        assertNotNull(apiKey);

        GeminiConfig config = new GeminiConfig();
        config.setApiKey(apiKey);
        config.setModel("gemini-2.5-flash");
        config.setTimeoutMs(15000);

        DefaultGeminiClient client = new DefaultGeminiClient(config);
        assertTrue(client.isConfigured());

        String systemInstruction = "You are a test assistant. Respond in JSON strictly: {\"status\": \"OK\"}";
        String userPrompt = "Ping test for DeadlineGuard AI backend integration.";

        String response = client.generateContent(systemInstruction, userPrompt);
        assertNotNull(response);
        assertTrue(response.contains("OK") || response.contains("status"));
    }
}
