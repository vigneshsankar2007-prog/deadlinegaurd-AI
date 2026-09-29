package com.deadlineguard.entity;

import com.deadlineguard.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing automated deadline reminders and academic alert notifications.
 * Mapped to table `notifications`.
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_read", columnList = "user_id, is_read, created_at"),
        @Index(name = "idx_notifications_task", columnList = "task_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "task"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Recipient student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    private Task task;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must not exceed 120 characters")
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @NotNull
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
