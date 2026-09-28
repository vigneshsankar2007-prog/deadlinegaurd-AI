package com.deadlineguard.dto.task;

import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public response DTO for Task deliverable data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String subjectColor;
    private String title;
    private String description;
    private TaskType taskType;
    private LocalDateTime deadline;
    private Integer difficulty;
    private BigDecimal academicWeight;
    private BigDecimal estimatedHours;
    private TaskStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(task.getId())
                .subjectId(task.getSubject() != null ? task.getSubject().getId() : null)
                .subjectName(task.getSubject() != null ? task.getSubject().getSubjectName() : null)
                .subjectCode(task.getSubject() != null ? task.getSubject().getSubjectCode() : null)
                .subjectColor(task.getSubject() != null ? task.getSubject().getColorHex() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .deadline(task.getDeadline())
                .difficulty(task.getDifficulty())
                .academicWeight(task.getAcademicWeight())
                .estimatedHours(task.getEstimatedHours())
                .status(task.getStatus())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
