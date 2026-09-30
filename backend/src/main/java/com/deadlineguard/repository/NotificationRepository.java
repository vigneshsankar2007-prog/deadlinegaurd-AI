package com.deadlineguard.repository;

import com.deadlineguard.entity.Notification;
import com.deadlineguard.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find a notification only if it belongs to the given student.
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /**
     * Check if a specific notification type was already issued for a student task.
     * Prevents duplicate deadline and urgency reminders.
     */
    boolean existsByUserIdAndTaskIdAndNotificationType(Long userId, Long taskId, NotificationType notificationType);

    /**
     * Check if a notification type (such as DAILY_SUMMARY) was already issued within a time window.
     */
    boolean existsByUserIdAndNotificationTypeAndCreatedAtBetween(
            Long userId,
            NotificationType notificationType,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Find all alerts for a student sorted newest first.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all unread alerts for a student.
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread notifications for badge counter.
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Mark all unread alerts as read for a student.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);
}
