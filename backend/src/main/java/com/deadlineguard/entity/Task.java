package com.deadlineguard.entity;

import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an academic task or deliverable (assignment, exam, project, lab, etc.).
 * Mapped to table `tasks`.
 *
 * Subject Ownership & Relationship Integrity:
 * - subject_id is nullable (extracurricular or general tasks do not require a course).
 * - Deleting a Subject sets subject_id to NULL, preventing accidental task deletion.
 * - Subject ownership is validated by the service layer: task.user.id must match subject.user.id.
 */
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_subject", columnList = "subject_id"),
        @Index(name = "idx_tasks_deadline", columnList = "deadline"),
        @Index(name = "idx_tasks_user_status_deadline", columnList = "user_id, status, deadline")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "subject", "taskPriority"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Owner student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = true)
    private Subject subject;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Task type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    @Builder.Default
    private TaskType taskType = TaskType.ASSIGNMENT;

    @NotNull(message = "Task deadline is required")
    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @NotNull(message = "Difficulty level is required")
    @Min(value = 1, message = "Difficulty must be at least 1 (Very Easy)")
    @Max(value = 5, message = "Difficulty cannot exceed 5 (Very Difficult)")
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @NotNull(message = "Academic weight is required")
    @DecimalMin(value = "0.00", message = "Academic weight must be non-negative")
    @DecimalMax(value = "100.00", message = "Academic weight cannot exceed 100.00%")
    @Column(name = "academic_weight", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal academicWeight = new BigDecimal("10.00");

    @NotNull(message = "Estimated hours are required")
    @DecimalMin(value = "0.1", message = "Estimated hours must be greater than 0.0")
    @Column(name = "estimated_hours", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal estimatedHours = new BigDecimal("1.0");

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TaskPriority taskPriority;
}
