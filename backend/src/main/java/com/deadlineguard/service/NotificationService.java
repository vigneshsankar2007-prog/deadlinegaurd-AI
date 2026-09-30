package com.deadlineguard.service;

import com.deadlineguard.dto.notification.NotificationResponse;
import com.deadlineguard.entity.Notification;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.NotificationType;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.NotificationRepository;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing automated deadline reminders, priority alerts, daily summaries,
 * and student notification read/unread management.
 *
 * ARCHITECTURAL CONTRACT:
 * - Duplicate Prevention: Each task event (48h, 24h, 6h, OVERDUE, HIGH_PRIORITY) is notified AT MOST ONCE.
 * - Daily Summary: At most one summary notification per student per calendar day.
 * - Stage 6 Priority Authority: Reuses TaskPriority calculations; does not compute independent scores.
 * - Multi-tenant isolation: Read and access operations require authenticated student ownership.
 * - Timezone: Strictly operates in UTC using injectable Clock for deterministic testing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final BigDecimal CRITICAL_PRIORITY_THRESHOLD = new BigDecimal("76.00");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    // =========================================================================
    // STUDENT NOTIFICATION RETRIEVAL & READ STATUS MANAGEMENT
    // =========================================================================

    /**
     * Retrieve all notifications for the authenticated student.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId, Boolean unreadOnly) {
        List<Notification> notifications;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific notification only if owned by the student.
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        return NotificationResponse.fromEntity(notification);
    }

    /**
     * Mark a single notification as read (only if owned by the student).
     */
    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        log.info("Marked notification id: {} as read for userId: {}", notificationId, userId);
        return NotificationResponse.fromEntity(saved);
    }

    /**
     * Mark all unread notifications as read for the authenticated student.
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadForUser(userId);
        log.info("Marked {} notifications as read for userId: {}", updatedCount, userId);
        return updatedCount;
    }

    /**
     * Count unread notifications for badge counters.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // =========================================================================
    // SCHEDULED BACKGROUND SCAN: DEADLINES & PRIORITY ALERTS
    // =========================================================================

    /**
     * Scans all incomplete deliverables across all students and generates:
     * - OVERDUE alerts
     * - 6-hour deadline reminders
     * - 24-hour deadline reminders
     * - 48-hour deadline reminders
     * - HIGH_PRIORITY / CRITICAL alerts
     *
     * Enforces strict duplicate prevention.
     */
    @Transactional
    public void processDeadlineReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        log.info("Executing deadline reminder scan at {}", now);

        List<Task> incompleteTasks = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());

        for (Task task : incompleteTasks) {
            try {
                processTaskAlerts(task, now);
            } catch (Exception e) {
                log.error("Failed processing alerts for taskId: {}: {}", task.getId(), e.getMessage());
            }
        }
    }

    private void processTaskAlerts(Task task, LocalDateTime now) {
        User user = task.getUser();
        if (user == null || task.getDeadline() == null) {
            return;
        }

        LocalDateTime deadline = task.getDeadline();
        String formattedDeadline = deadline.format(DATE_TIME_FORMATTER);

        // 1. OVERDUE check
        if (deadline.isBefore(now)) {
            if (!notificationRepository.existsByUserIdAndTaskIdAndNotificationType(user.getId(), task.getId(), NotificationType.OVERDUE)) {
                createAndSaveNotification(
                        user,
                        task,
                        "Task Overdue",
                        String.format("\"%s\" passed its deadline on %s and is still incomplete.", task.getTitle(), formattedDeadline),
                        NotificationType.OVERDUE
                );
            }
            return; // Overdue tasks do not trigger upcoming countdown reminders
        }

        // Upcoming deadline countdowns
        double hoursRemaining = Duration.between(now, deadline).toSeconds() / 3600.0;

        // 2. DEADLINE_6H check (due within 6 hours)
        if (hoursRemaining <= 6.0) {
            if (!notificationRepository.existsByUserIdAndTaskIdAndNotificationType(user.getId(), task.getId(), NotificationType.DEADLINE_6H)) {
                createAndSaveNotification(
                        user,
                        task,
                        "Deadline in 6 Hours",
                        String.format("Urgent: \"%s\" is due in approximately 6 hours (%s). Wrap up final submissions.", task.getTitle(), formattedDeadline),
                        NotificationType.DEADLINE_6H
                );
            }
        }

        // 3. DEADLINE_24H check (due within 24 hours)
        if (hoursRemaining <= 24.0) {
            if (!notificationRepository.existsByUserIdAndTaskIdAndNotificationType(user.getId(), task.getId(), NotificationType.DEADLINE_24H)) {
                createAndSaveNotification(
                        user,
                        task,
                        "Deadline in 24 Hours",
                        String.format("\"%s\" is due in approximately 24 hours (%s). Review remaining work and complete your next study block.", task.getTitle(), formattedDeadline),
                        NotificationType.DEADLINE_24H
                );
            }
        }

        // 4. DEADLINE_48H check (due within 48 hours)
        if (hoursRemaining <= 48.0) {
            if (!notificationRepository.existsByUserIdAndTaskIdAndNotificationType(user.getId(), task.getId(), NotificationType.DEADLINE_48H)) {
                createAndSaveNotification(
                        user,
                        task,
                        "Deadline in 48 Hours",
                        String.format("\"%s\" is due in approximately 48 hours (%s). Review remaining deliverables and schedule your focus session.", task.getTitle(), formattedDeadline),
                        NotificationType.DEADLINE_48H
                );
            }
        }

        // 5. HIGH_PRIORITY alert check (Stage 6 authoritative calculation)
        TaskPriority priority = taskPriorityRepository.findByTaskId(task.getId()).orElse(null);
        if (priority != null) {
            boolean isCritical = priority.getPriorityLevel() == PriorityLevel.CRITICAL
                    || (priority.getPriorityScore() != null && priority.getPriorityScore().compareTo(CRITICAL_PRIORITY_THRESHOLD) >= 0);

            if (isCritical && !notificationRepository.existsByUserIdAndTaskIdAndNotificationType(user.getId(), task.getId(), NotificationType.HIGH_PRIORITY)) {
                createAndSaveNotification(
                        user,
                        task,
                        "High-Priority Task Alert",
                        String.format("\"%s\" currently has a %s priority score (%s). Immediate focus recommended.",
                                task.getTitle(), priority.getPriorityLevel(), priority.getPriorityScore()),
                        NotificationType.HIGH_PRIORITY
                );
            }
        }
    }

    // =========================================================================
    // SCHEDULED BACKGROUND SCAN: DAILY SUMMARY
    // =========================================================================

    /**
     * Generates a daily academic deliverable digest for each registered student.
     * Enforces strict once-per-day generation per student.
     */
    @Transactional
    public void processDailySummaries() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        log.info("Executing daily summary notification generation for {}", today);

        List<User> students = userRepository.findAll();
        for (User student : students) {
            try {
                // Duplicate prevention: exactly once per day per student
                boolean alreadySentToday = notificationRepository.existsByUserIdAndNotificationTypeAndCreatedAtBetween(
                        student.getId(),
                        NotificationType.DAILY_SUMMARY,
                        startOfDay,
                        endOfDay
                );

                if (alreadySentToday) {
                    continue;
                }

                generateDailySummaryForStudent(student, now);

            } catch (Exception e) {
                log.error("Failed creating daily summary for userId: {}: {}", student.getId(), e.getMessage());
            }
        }
    }

    private void generateDailySummaryForStudent(User student, LocalDateTime now) {
        List<Task> activeTasks = taskRepository.findByUserIdOrderByDeadlineAsc(student.getId()).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());

        long overdueCount = activeTasks.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(now))
                .count();

        long criticalCount = activeTasks.stream()
                .map(t -> taskPriorityRepository.findByTaskId(t.getId()).orElse(null))
                .filter(p -> p != null && (p.getPriorityLevel() == PriorityLevel.CRITICAL
                        || (p.getPriorityScore() != null && p.getPriorityScore().compareTo(CRITICAL_PRIORITY_THRESHOLD) >= 0)))
                .count();

        String nearestDeadline = activeTasks.isEmpty()
                ? "No pending deadlines"
                : (activeTasks.get(0).getDeadline() != null
                ? activeTasks.get(0).getDeadline().format(DATE_TIME_FORMATTER)
                : "None");

        String message = String.format(
                "You have %d pending deliverable(s), %d overdue, and %d critical priority task(s). Nearest deadline: %s.",
                activeTasks.size(), overdueCount, criticalCount, nearestDeadline
        );

        createAndSaveNotification(
                student,
                null,
                "Daily Academic Summary",
                message,
                NotificationType.DAILY_SUMMARY
        );
    }

    private void createAndSaveNotification(
            User user,
            Task task,
            String title,
            String message,
            NotificationType type
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .task(task)
                .title(title)
                .message(message)
                .notificationType(type)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created {} notification id: {} for userId: {} (taskId: {})",
                type, saved.getId(), user.getId(), task != null ? task.getId() : "N/A");
    }
}
