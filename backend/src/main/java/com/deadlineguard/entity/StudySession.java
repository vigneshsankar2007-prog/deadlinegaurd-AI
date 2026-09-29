package com.deadlineguard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing student focus timer and Pomodoro sessions.
 * Mapped to table `study_sessions`.
 */
@Entity
@Table(
    name = "study_sessions",
    indexes = {
        @Index(name = "idx_sessions_user_start", columnList = "user_id, start_time"),
        @Index(name = "idx_sessions_task", columnList = "task_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "task"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StudySession {

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
    @JoinColumn(name = "task_id", nullable = true)
    private Task task;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 0, message = "Duration cannot be negative")
    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 0;

    @NotNull
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    @Column(name = "notes", length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
