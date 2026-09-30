package com.deadlineguard.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Subject-level academic workload and productivity breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectAnalyticsResponse {

    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String colorHex;
    private Long taskCount;
    private Long completedTaskCount;
    private Long pendingTaskCount;
    private Long overdueTaskCount;
    private Double estimatedWorkloadHours;
    private Integer actualStudyMinutes;
    private Double completionPercentage;
}
