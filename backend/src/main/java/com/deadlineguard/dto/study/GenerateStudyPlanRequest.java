package com.deadlineguard.dto.study;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request payload for generating a time-blocked AI Study Plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateStudyPlanRequest {

    @NotNull(message = "availableHours is required")
    @DecimalMin(value = "0.25", message = "availableHours must be at least 0.25 hours (15 minutes)")
    @DecimalMax(value = "24.0", message = "availableHours cannot exceed 24.0 hours")
    private Double availableHours;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "breakDurationMinutes is required")
    @Min(value = 0, message = "breakDurationMinutes cannot be negative")
    @Max(value = 180, message = "breakDurationMinutes cannot exceed 180 minutes")
    @Builder.Default
    private Integer breakDurationMinutes = 15;
}
