package com.deadlineguard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity storing daily aggregated student productivity metrics for analytics and trend tracking.
 * Mapped to table `productivity_stats`.
 * Unique per student per date: (user_id, stat_date).
 */
@Entity
@Table(
    name = "productivity_stats",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_stat_date", columnNames = {"user_id", "stat_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductivityStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Owner student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotNull(message = "Stat date is required")
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @NotNull
    @Min(0)
    @Column(name = "tasks_completed", nullable = false)
    @Builder.Default
    private Integer tasksCompleted = 0;

    @NotNull
    @Min(0)
    @Column(name = "tasks_overdue", nullable = false)
    @Builder.Default
    private Integer tasksOverdue = 0;

    @NotNull
    @Min(0)
    @Column(name = "study_minutes", nullable = false)
    @Builder.Default
    private Integer studyMinutes = 0;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "productivity_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal productivityScore = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
