package com.deadlineguard.service.ai;

import com.deadlineguard.config.GeminiConfig;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Official Google Gen AI Java SDK client implementation for Gemini models.
 * Strictly respects timeouts and isolates network calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultGeminiClient implements GeminiClient {

    private final GeminiConfig geminiConfig;

    @Override
    public boolean isConfigured() {
        return geminiConfig.getApiKey() != null && !geminiConfig.getApiKey().trim().isEmpty();
    }

    @Override
    public String generateContent(String systemInstruction, String userPrompt) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Google Gemini API key is not configured. Set GOOGLE_API_KEY environment variable.");
        }

        log.info("Sending request to Google Gemini API (model: {})", geminiConfig.getModel());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            Client client = Client.builder()
                    .apiKey(geminiConfig.getApiKey().trim())
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
                    .responseMimeType("application/json")
                    .temperature(0.2) // Low temperature for deterministic, grounded reasoning
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    geminiConfig.getModel(),
                    userPrompt,
                    config
            );

            return response.text();
        });

        try {
            return future.get(geminiConfig.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Gemini API call timed out after {}ms", geminiConfig.getTimeoutMs());
            throw new TimeoutException("Gemini API call timed out after " + geminiConfig.getTimeoutMs() + "ms");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Gemini API call failed with execution error: {}", cause != null ? cause.getMessage() : e.getMessage());
            throw (cause instanceof Exception) ? (Exception) cause : new RuntimeException(cause);
        } finally {
            executor.shutdownNow();
        }
    }
}
