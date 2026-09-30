package com.deadlineguard.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public response DTO exposing structured AI Study Plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanResponse {

    private String planDate;
    private Double availableHours;
    private LocalDateTime startTime;
    private Integer breakDurationMinutes;
    private List<StudySessionBlockDto> sessions;
    private Integer totalStudyMinutes;
    private Integer totalBreakMinutes;
    private Boolean fallbackUsed;
    private Long recommendationId;
    private String message;
}
