package com.deadlineguard.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Single calendar date data point for student weekly productivity trends.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAnalyticsPoint {

    private LocalDate date;
    private Integer tasksCompleted;
    private Integer tasksOverdue;
    private Integer studyMinutes;
    private BigDecimal productivityScore;
}
