package com.deadlineguard.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a single focused study block or deliverable session in the study plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionBlockDto {

    private Long taskId;
    private String title;
    private String subjectCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String reason;
}
