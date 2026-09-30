package com.deadlineguard.dto.ai;

import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Contextual task snapshot supplied strictly to Gemini AI for reasoning.
 * Contains only academic deliverable and deterministic priority context.
 * NEVER contains passwords, hashes, tokens, or security credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAiContextDto {

    private Long taskId;
    private String subjectCode;
    private String subjectName;
    private String title;
    private String description;
    private TaskType taskType;
    private LocalDateTime deadline;
    private Double hoursRemaining;
    private Integer difficulty;
    private BigDecimal academicWeight;
    private BigDecimal estimatedHours;
    private TaskStatus status;

    // Authoritative deterministic priority attributes (from Stage 6)
    private BigDecimal priorityScore;
    private PriorityLevel priorityTier;
    private BigDecimal urgencyScore;
    private BigDecimal difficultyScore;
    private BigDecimal weightScore;
    private BigDecimal effortScore;
    private BigDecimal workloadScore;

    public static TaskAiContextDto from(Task task, TaskPriority priority, LocalDateTime now) {
        double hoursRemaining = 0.0;
        if (task.getDeadline() != null) {
            hoursRemaining = Math.max(0.0, Duration.between(now, task.getDeadline()).toSeconds() / 3600.0);
        }

        return TaskAiContextDto.builder()
                .taskId(task.getId())
                .subjectCode(task.getSubject() != null ? task.getSubject().getSubjectCode() : null)
                .subjectName(task.getSubject() != null ? task.getSubject().getSubjectName() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .deadline(task.getDeadline())
                .hoursRemaining(Math.round(hoursRemaining * 100.0) / 100.0)
                .difficulty(task.getDifficulty())
                .academicWeight(task.getAcademicWeight())
                .estimatedHours(task.getEstimatedHours())
                .status(task.getStatus())
                .priorityScore(priority != null ? priority.getPriorityScore() : null)
                .priorityTier(priority != null ? priority.getPriorityLevel() : null)
                .urgencyScore(priority != null ? priority.getUrgencyScore() : null)
                .difficultyScore(priority != null ? priority.getDifficultyScore() : null)
                .weightScore(priority != null ? priority.getWeightScore() : null)
                .effortScore(priority != null ? priority.getEffortScore() : null)
                .workloadScore(priority != null ? priority.getWorkloadScore() : null)
                .build();
    }
}
