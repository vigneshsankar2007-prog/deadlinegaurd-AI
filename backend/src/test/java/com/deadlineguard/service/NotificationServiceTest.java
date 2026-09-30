package com.deadlineguard.service;

import com.deadlineguard.entity.Notification;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.NotificationType;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.NotificationRepository;
import com.deadlineguard.repository.TaskPriorityRepository;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Stage 9: NotificationService unit and integration tests.
 * Uses an injected fixed Clock to test reminder thresholds with complete determinism.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private Clock clock;

    private User userA;
    private User userB;
    private final Instant fixedInstant = Instant.parse("2026-10-01T12:00:00Z");
    private final LocalDateTime nowTime = LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        notificationRepository.deleteAll();
        taskPriorityRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(User.builder()
                .name("Alice Cooper")
                .email("alice@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Computer Science")
                .semester(5)
                .college("Engineering College")
                .build());

        userB = userRepository.save(User.builder()
                .name("Bob Marley")
                .email("bob@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Mechanical Engineering")
                .semester(3)
                .college("Engineering College")
                .build());
    }

    @Test
    @DisplayName("8. 48-hour deadline reminder is generated")
    void deadline48hReminder_isGenerated() {
        // Deadline in 36 hours (within the 48-hour window)
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Operating Systems Project")
                .taskType(TaskType.PROJECT)
                .deadline(nowTime.plusHours(36))
                .status(TaskStatus.PENDING)
                .difficulty(3)
                .build());

        notificationService.processDeadlineReminders();

        assertTrue(notificationRepository.existsByUserIdAndTaskIdAndNotificationType(
                userA.getId(), task.getId(), NotificationType.DEADLINE_48H));
    }

    @Test
    @DisplayName("9. 24-hour deadline reminder is generated")
    void deadline24hReminder_isGenerated() {
        // Deadline in 20 hours (within the 24-hour window)
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Algorithms Homework")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.plusHours(20))
                .status(TaskStatus.IN_PROGRESS)
                .difficulty(4)
                .build());

        notificationService.processDeadlineReminders();

        assertTrue(notificationRepository.existsByUserIdAndTaskIdAndNotificationType(
                userA.getId(), task.getId(), NotificationType.DEADLINE_24H));
    }

    @Test
    @DisplayName("10. 6-hour deadline reminder is generated")
    void deadline6hReminder_isGenerated() {
        // Deadline in 4 hours (within the 6-hour window)
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Calculus Quiz Prep")
                .taskType(TaskType.QUIZ)
                .deadline(nowTime.plusHours(4))
                .status(TaskStatus.PENDING)
                .difficulty(2)
                .build());

        notificationService.processDeadlineReminders();

        assertTrue(notificationRepository.existsByUserIdAndTaskIdAndNotificationType(
                userA.getId(), task.getId(), NotificationType.DEADLINE_6H));
    }

    @Test
    @DisplayName("11. Overdue notification is generated for overdue incomplete task")
    void overdueNotification_isGenerated() {
        // Deadline in the past (2 hours ago)
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Physics Lab Report")
                .taskType(TaskType.LAB)
                .deadline(nowTime.minusHours(2))
                .status(TaskStatus.PENDING)
                .difficulty(3)
                .build());

        notificationService.processDeadlineReminders();

        assertTrue(notificationRepository.existsByUserIdAndTaskIdAndNotificationType(
                userA.getId(), task.getId(), NotificationType.OVERDUE));
    }

    @Test
    @DisplayName("12. Completed task does not receive deadline or overdue reminders")
    void completedTask_doesNotReceiveDeadlineReminder() {
        taskRepository.save(Task.builder()
                .user(userA)
                .title("Completed Project")
                .taskType(TaskType.PROJECT)
                .deadline(nowTime.minusHours(1))
                .status(TaskStatus.COMPLETED)
                .difficulty(4)
                .build());

        taskRepository.save(Task.builder()
                .user(userA)
                .title("Completed Assignment")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.plusHours(4))
                .status(TaskStatus.COMPLETED)
                .difficulty(3)
                .build());

        notificationService.processDeadlineReminders();

        assertEquals(0, notificationRepository.count());
    }

    @Test
    @DisplayName("13 & 14 & 15. High priority notification is generated using existing Stage 6 priority")
    void highPriorityNotification_isGeneratedForQualifyingTask() {
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Compiler Construction Final Project")
                .taskType(TaskType.PROJECT)
                .deadline(nowTime.plusDays(3))
                .status(TaskStatus.IN_PROGRESS)
                .difficulty(5)
                .build());

        // Stage 6 priority record (CRITICAL priority tier)
        taskPriorityRepository.save(TaskPriority.builder()
                .task(task)
                .priorityScore(new BigDecimal("82.50"))
                .priorityLevel(PriorityLevel.CRITICAL)
                .urgencyScore(new BigDecimal("70.00"))
                .difficultyScore(new BigDecimal("90.00"))
                .weightScore(new BigDecimal("85.00"))
                .effortScore(new BigDecimal("80.00"))
                .workloadScore(new BigDecimal("75.00"))
                .explanationText("High priority due to difficulty and weight")
                .calculatedAt(nowTime)
                .build());

        notificationService.processDeadlineReminders();

        assertTrue(notificationRepository.existsByUserIdAndTaskIdAndNotificationType(
                userA.getId(), task.getId(), NotificationType.HIGH_PRIORITY));
    }

    @Test
    @DisplayName("16-20. Duplicate reminders are not created when scheduler runs repeatedly")
    void duplicateReminders_areNotCreated() {
        Task task48 = taskRepository.save(Task.builder()
                .user(userA)
                .title("Deliverable 48")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.plusHours(30))
                .status(TaskStatus.PENDING)
                .build());

        Task taskOverdue = taskRepository.save(Task.builder()
                .user(userA)
                .title("Deliverable Overdue")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.minusHours(5))
                .status(TaskStatus.PENDING)
                .build());

        // Run scheduler 3 times in a row
        notificationService.processDeadlineReminders();
        notificationService.processDeadlineReminders();
        notificationService.processDeadlineReminders();

        List<Notification> user48Notifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userA.getId()).stream()
                .filter(n -> n.getNotificationType() == NotificationType.DEADLINE_48H && n.getTask().getId().equals(task48.getId()))
                .toList();

        List<Notification> overdueNotifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userA.getId()).stream()
                .filter(n -> n.getNotificationType() == NotificationType.OVERDUE && n.getTask().getId().equals(taskOverdue.getId()))
                .toList();

        assertEquals(1, user48Notifs.size(), "DEADLINE_48H should be generated exactly once");
        assertEquals(1, overdueNotifs.size(), "OVERDUE should be generated exactly once");
    }

    @Test
    @DisplayName("21 & 23. Daily summary is generated once per day per user")
    void duplicateDailySummary_isNotCreatedWithinSameDay() {
        taskRepository.save(Task.builder()
                .user(userA)
                .title("Active Task")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.plusDays(2))
                .status(TaskStatus.PENDING)
                .build());

        // Run daily summary twice on the same day
        notificationService.processDailySummaries();
        notificationService.processDailySummaries();

        List<Notification> summaries = notificationRepository.findByUserIdOrderByCreatedAtDesc(userA.getId()).stream()
                .filter(n -> n.getNotificationType() == NotificationType.DAILY_SUMMARY)
                .toList();

        assertEquals(1, summaries.size(), "Daily summary must be created at most once per calendar day");
    }

    @Test
    @DisplayName("22. Daily summary contains actual task counts")
    void dailySummary_containsActualTaskCounts() {
        // 1 overdue task
        taskRepository.save(Task.builder()
                .user(userA)
                .title("Overdue Task")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.minusHours(3))
                .status(TaskStatus.PENDING)
                .build());

        // 1 pending task
        Task critTask = taskRepository.save(Task.builder()
                .user(userA)
                .title("Critical Task")
                .taskType(TaskType.PROJECT)
                .deadline(nowTime.plusHours(10))
                .status(TaskStatus.IN_PROGRESS)
                .build());

        taskPriorityRepository.save(TaskPriority.builder()
                .task(critTask)
                .priorityScore(new BigDecimal("88.00"))
                .priorityLevel(PriorityLevel.CRITICAL)
                .explanationText("Critical")
                .urgencyScore(BigDecimal.TEN)
                .difficultyScore(BigDecimal.TEN)
                .weightScore(BigDecimal.TEN)
                .effortScore(BigDecimal.TEN)
                .workloadScore(BigDecimal.TEN)
                .calculatedAt(nowTime)
                .build());

        notificationService.processDailySummaries();

        Notification summary = notificationRepository.findByUserIdOrderByCreatedAtDesc(userA.getId()).stream()
                .filter(n -> n.getNotificationType() == NotificationType.DAILY_SUMMARY)
                .findFirst()
                .orElse(null);

        assertNotNull(summary);
        // Message should contain 2 pending deliverable(s), 1 overdue, and 1 critical
        assertTrue(summary.getMessage().contains("2 pending deliverable(s)"));
        assertTrue(summary.getMessage().contains("1 overdue"));
        assertTrue(summary.getMessage().contains("1 critical"));
    }

    @Test
    @DisplayName("24 & 25. Notification user strictly matches task user (no cross-user leakage)")
    void scheduledNotification_cannotCrossUserTaskOwnership() {
        // Bob's task
        Task bobsTask = taskRepository.save(Task.builder()
                .user(userB)
                .title("Bob's Thermodynamics Homework")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(nowTime.plusHours(20))
                .status(TaskStatus.PENDING)
                .build());

        notificationService.processDeadlineReminders();

        List<Notification> aliceNotifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userA.getId());
        List<Notification> bobNotifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userB.getId());

        assertEquals(0, aliceNotifs.size());
        assertEquals(1, bobNotifs.size());
        assertEquals(userB.getId(), bobNotifs.get(0).getUser().getId());
        assertEquals(bobsTask.getId(), bobNotifs.get(0).getTask().getId());
    }

    @Test
    @DisplayName("26. Scheduler handles no tasks safely")
    void schedulerHandlesNoTasksSafely() {
        assertDoesNotThrow(() -> notificationService.processDeadlineReminders());
        assertDoesNotThrow(() -> notificationService.processDailySummaries());
    }
}
