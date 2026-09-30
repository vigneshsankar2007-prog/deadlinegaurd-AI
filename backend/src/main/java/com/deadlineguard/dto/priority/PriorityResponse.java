package com.deadlineguard.dto.priority;

import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.enums.PriorityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public response DTO exposing the explainable 5-factor task priority breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private BigDecimal priorityScore;
    private PriorityLevel priorityTier;
    private PriorityLevel priorityLevel;
    private BigDecimal urgencyScore;
    private BigDecimal difficultyScore;
    private BigDecimal academicWeightScore;
    private BigDecimal weightScore;
    private BigDecimal effortScore;
    private BigDecimal workloadScore;
    private String explanationText;
    private LocalDateTime calculatedAt;

    public static PriorityResponse fromEntity(TaskPriority priority) {
        if (priority == null) {
            return null;
        }
        return PriorityResponse.builder()
                .id(priority.getId())
                .taskId(priority.getTask() != null ? priority.getTask().getId() : null)
                .taskTitle(priority.getTask() != null ? priority.getTask().getTitle() : null)
                .priorityScore(priority.getPriorityScore())
                .priorityTier(priority.getPriorityLevel())
                .priorityLevel(priority.getPriorityLevel())
                .urgencyScore(priority.getUrgencyScore())
                .difficultyScore(priority.getDifficultyScore())
                .academicWeightScore(priority.getWeightScore())
                .weightScore(priority.getWeightScore())
                .effortScore(priority.getEffortScore())
                .workloadScore(priority.getWorkloadScore())
                .explanationText(priority.getExplanationText())
                .calculatedAt(priority.getCalculatedAt())
                .build();
    }
}
