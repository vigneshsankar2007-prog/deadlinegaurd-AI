package com.deadlineguard.entity;

import com.deadlineguard.entity.enums.PriorityLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entity storing explainable 5-factor task priority calculation results.
 * Mapped to table `task_priorities`.
 *
 * Formula:
 *   priority_score = 0.40 * urgency_score
 *                  + 0.20 * difficulty_score
 *                  + 0.20 * weight_score
 *                  + 0.10 * effort_score
 *                  + 0.10 * workload_score
 */
@Entity
@Table(
    name = "task_priorities",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_priorities_task", columnNames = {"task_id"})
    },
    indexes = {
        @Index(name = "idx_priorities_score_level", columnList = "priority_score, priority_level")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"task"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TaskPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Associated task is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    @JsonIgnore
    private Task task;

    @NotNull(message = "Priority score is required")
    @DecimalMin(value = "0.00", message = "Priority score must be >= 0.00")
    @DecimalMax(value = "100.00", message = "Priority score must be <= 100.00")
    @Column(name = "priority_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal priorityScore;

    @NotNull(message = "Priority level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel;

    @NotNull(message = "Urgency score is required")
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "urgency_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal urgencyScore;

    @NotNull(message = "Difficulty score is required")
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "difficulty_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal difficultyScore;

    @NotNull(message = "Academic weight score is required")
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "weight_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightScore;

    @NotNull(message = "Effort score is required")
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "effort_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal effortScore;

    @NotNull(message = "Workload score is required")
    @DecimalMin("0.00") @DecimalMax("100.00")
    @Column(name = "workload_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal workloadScore;

    @NotBlank(message = "Explanation text is required")
    @Column(name = "explanation_text", nullable = false, columnDefinition = "TEXT")
    private String explanationText;

    @NotNull(message = "Calculation timestamp is required")
    @Column(name = "calculated_at", nullable = false)
    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
