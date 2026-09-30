package com.deadlineguard.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured output JSON schema mapping for Gemini AI recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiRecommendationSchema {

    private String recommendationType;
    private String recommendationText;
    private String aiReasoning;
    private String suggestedAction;
    private Long taskId;
}
