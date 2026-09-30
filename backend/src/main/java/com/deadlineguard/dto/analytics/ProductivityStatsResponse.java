package com.deadlineguard.dto.analytics;

import com.deadlineguard.entity.ProductivityStat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Public response DTO for individual ProductivityStat records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductivityStatsResponse {

    private Long id;
    private Long userId;
    private LocalDate statDate;
    private Integer tasksCompleted;
    private Integer tasksOverdue;
    private Integer studyMinutes;
    private BigDecimal productivityScore;
    private LocalDateTime createdAt;

    public static ProductivityStatsResponse fromEntity(ProductivityStat entity) {
        if (entity == null) {
            return null;
        }
        return ProductivityStatsResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .statDate(entity.getStatDate())
                .tasksCompleted(entity.getTasksCompleted())
                .tasksOverdue(entity.getTasksOverdue())
                .studyMinutes(entity.getStudyMinutes())
                .productivityScore(entity.getProductivityScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
