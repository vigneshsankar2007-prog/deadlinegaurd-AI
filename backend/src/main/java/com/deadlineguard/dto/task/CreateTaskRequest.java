package com.deadlineguard.dto.task;

import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request payload for creating a new task / deliverable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    /**
     * Optional ID of the enrolled subject. If provided, must belong to the authenticated student.
     * Can be null for general or extracurricular tasks.
     */
    private Long subjectId;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Task type is required")
    @Builder.Default
    private TaskType taskType = TaskType.ASSIGNMENT;

    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;

    @NotNull(message = "Difficulty level is required")
    @Min(value = 1, message = "Difficulty must be between 1 (Very Easy) and 5 (Very Difficult)")
    @Max(value = 5, message = "Difficulty must be between 1 (Very Easy) and 5 (Very Difficult)")
    @Builder.Default
    private Integer difficulty = 3;

    @NotNull(message = "Academic weight is required")
    @DecimalMin(value = "0.00", message = "Academic weight must be at least 0.00%")
    @DecimalMax(value = "100.00", message = "Academic weight cannot exceed 100.00%")
    @Builder.Default
    private BigDecimal academicWeight = new BigDecimal("10.00");

    @NotNull(message = "Estimated hours are required")
    @DecimalMin(value = "0.1", message = "Estimated hours must be greater than 0.0")
    @Builder.Default
    private BigDecimal estimatedHours = new BigDecimal("1.0");

    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
}
