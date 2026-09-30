package com.deadlineguard.scheduler;

import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.NotificationType;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.NotificationRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Stage 9: Scheduler component tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSchedulerTest {

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private Clock clock;

    private User user;
    private final Instant fixedInstant = Instant.parse("2026-10-01T12:00:00Z");
    private final LocalDateTime nowTime = LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .name("Student Alice")
                .email("alice@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Computer Science")
                .semester(5)
                .college("Engineering College")
                .build());
    }

    @Test
    @DisplayName("27. Scheduler can run repeatedly without duplicate notifications")
    void schedulerCanRunRepeatedlyWithoutDuplicateNotifications() {
        taskRepository.save(Task.builder()
                .user(user)
                .title("Database Systems Midterm")
                .taskType(TaskType.EXAM)
                .deadline(nowTime.plusHours(22))
                .status(TaskStatus.PENDING)
                .difficulty(4)
                .build());

        // Execute scan multiple times
        notificationScheduler.runDeadlineScan();
        notificationScheduler.runDeadlineScan();
        notificationScheduler.runDailySummary();
        notificationScheduler.runDailySummary();

        long count24 = notificationRepository.findAll().stream()
                .filter(n -> n.getNotificationType() == NotificationType.DEADLINE_24H)
                .count();

        long countDaily = notificationRepository.findAll().stream()
                .filter(n -> n.getNotificationType() == NotificationType.DAILY_SUMMARY)
                .count();

        assertEquals(1, count24, "Should create exactly 1 DEADLINE_24H alert");
        assertEquals(1, countDaily, "Should create exactly 1 DAILY_SUMMARY alert");
    }

    @Test
    @DisplayName("28. Scheduler processes relevant deadline windows")
    void schedulerProcessesRelevantDeadlineWindows() {
        // Task in 5 hours (qualifies for both 6h, 24h, 48h windows)
        taskRepository.save(Task.builder()
                .user(user)
                .title("Immediate Deliverable")
                .taskType(TaskType.LAB)
                .deadline(nowTime.plusHours(5))
                .status(TaskStatus.PENDING)
                .difficulty(3)
                .build());

        notificationScheduler.runDeadlineScan();

        var alerts = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        // All qualifying threshold reminders for this task window should be triggered
        assertTrue(alerts.stream().anyMatch(n -> n.getNotificationType() == NotificationType.DEADLINE_6H));
        assertTrue(alerts.stream().anyMatch(n -> n.getNotificationType() == NotificationType.DEADLINE_24H));
        assertTrue(alerts.stream().anyMatch(n -> n.getNotificationType() == NotificationType.DEADLINE_48H));
    }
}
