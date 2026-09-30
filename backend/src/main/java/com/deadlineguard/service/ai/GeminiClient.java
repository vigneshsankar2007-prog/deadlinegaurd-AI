package com.deadlineguard.service.ai;

/**
 * Abstraction for Google Gemini generative AI client.
 * Decouples AIService from direct network dependencies, enabling seamless mocking in unit/integration tests.
 */
public interface GeminiClient {

    /**
     * Generate content with system instruction and user prompt, expecting structured JSON output.
     *
     * @param systemInstruction Behavioral persona and schema constraints
     * @param userPrompt Grounded task and priority context
     * @return Raw JSON response string from Gemini
     * @throws Exception if API key is missing, network error, timeout, or rate-limited
     */
    String generateContent(String systemInstruction, String userPrompt) throws Exception;

    /**
     * Check if a valid API key is present and configured.
     */
    boolean isConfigured();
}
