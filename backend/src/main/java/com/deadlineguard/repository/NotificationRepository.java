package com.deadlineguard.repository;

import com.deadlineguard.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

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
