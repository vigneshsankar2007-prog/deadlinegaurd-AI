package com.deadlineguard.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main dashboard productivity overview response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardResponse {

    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Long overdueTasks;
    private Double completionPercentage;
    private Double overdueRate;
    private Integer totalStudyMinutes;
    private Double totalStudyHours;
    private Double averageSessionMinutes;
    private BigDecimal currentProductivityScore;
    private Integer weeklyStudyMinutes;
    private Integer weeklyCompletedTasks;
    private Integer weeklyOverdueTasks;
    private Long activeTaskCount;
    private Long criticalTaskCount;
    private List<SubjectAnalyticsResponse> subjects;
}
