package com.deadlineguard.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Weekly productivity statistics response over the 7-day trailing window.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAnalyticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWeeklyStudyMinutes;
    private Integer totalWeeklyTasksCompleted;
    private Integer totalWeeklyTasksOverdue;
    private BigDecimal averageProductivityScore;
    private List<DailyAnalyticsPoint> dailyPoints;
}
