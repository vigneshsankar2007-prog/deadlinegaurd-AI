package com.deadlineguard.dto.ai;

import com.deadlineguard.entity.AIRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Public response DTO exposing Gemini AI student recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {

    private Long id;
    private Long userId;
    private Long taskId;
    private String taskTitle;
    private String recommendationType;
    private String recommendationText;
    private String aiReasoning;
    private String suggestedAction;
    private Boolean fallbackUsed;
    private LocalDateTime generatedAt;

    public static AIRecommendationResponse fromEntity(AIRecommendation entity) {
        return fromEntity(entity, false);
    }

    public static AIRecommendationResponse fromEntity(AIRecommendation entity, boolean fallbackUsed) {
        if (entity == null) {
            return null;
        }
        return AIRecommendationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskTitle(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .recommendationType(entity.getRecommendationType())
                .recommendationText(entity.getRecommendationText())
                .aiReasoning(entity.getAiReasoning())
                .suggestedAction(entity.getSuggestedAction())
                .fallbackUsed(fallbackUsed)
                .generatedAt(entity.getGeneratedAt())
                .build();
    }
}
