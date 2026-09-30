package com.deadlineguard.service;

import com.deadlineguard.dto.analytics.AnalyticsDashboardResponse;
import com.deadlineguard.dto.analytics.SubjectAnalyticsResponse;
import com.deadlineguard.dto.analytics.WeeklyAnalyticsResponse;
import com.deadlineguard.entity.*;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.*;
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
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Stage 10: AnalyticsService unit and integration tests.
 * Injects a fixed Clock for deterministic UTC date/time calculations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private ProductivityStatRepository productivityStatRepository;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private Clock clock;

    private User userA;
    private User userB;
    private Subject subjectCS;
    private final Instant fixedInstant = Instant.parse("2026-10-07T12:00:00Z");
    private final LocalDateTime nowTime = LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC);
    private final LocalDate todayDate = LocalDate.of(2026, 10, 7);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        productivityStatRepository.deleteAll();
        studySessionRepository.deleteAll();
        taskPriorityRepository.deleteAll();
        taskRepository.deleteAll();
        subjectRepository.deleteAll();
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

        subjectCS = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Algorithms & Data Structures")
                .subjectCode("CS201")
                .credits(4)
                .colorHex("#3B82F6")
                .build());
    }

    @Test
    @DisplayName("5. Dashboard calculates completion percentage correctly")
    void dashboardCalculatesCompletionPercentageCorrectly() {
        // 3 completed, 1 pending => 75.0%
        taskRepository.save(Task.builder().user(userA).title("Task 1").status(TaskStatus.COMPLETED).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(1)).build());
        taskRepository.save(Task.builder().user(userA).title("Task 2").status(TaskStatus.COMPLETED).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(1)).build());
        taskRepository.save(Task.builder().user(userA).title("Task 3").status(TaskStatus.COMPLETED).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(1)).build());
        taskRepository.save(Task.builder().user(userA).title("Task 4").status(TaskStatus.PENDING).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(1)).build());

        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(4, resp.getTotalTasks());
        assertEquals(3, resp.getCompletedTasks());
        assertEquals(75.0, resp.getCompletionPercentage());
    }

    @Test
    @DisplayName("6. Dashboard calculates overdue rate correctly")
    void dashboardCalculatesOverdueRateCorrectly() {
        // 2 overdue (deadline in past and not completed), 2 future pending => total 4 => 50.0% overdue
        taskRepository.save(Task.builder().user(userA).title("Overdue 1").status(TaskStatus.PENDING).taskType(TaskType.ASSIGNMENT).deadline(nowTime.minusHours(5)).build());
        taskRepository.save(Task.builder().user(userA).title("Overdue 2").status(TaskStatus.IN_PROGRESS).taskType(TaskType.ASSIGNMENT).deadline(nowTime.minusDays(1)).build());
        taskRepository.save(Task.builder().user(userA).title("Future 1").status(TaskStatus.PENDING).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(2)).build());
        taskRepository.save(Task.builder().user(userA).title("Future 2").status(TaskStatus.PENDING).taskType(TaskType.ASSIGNMENT).deadline(nowTime.plusDays(3)).build());

        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(4, resp.getTotalTasks());
        assertEquals(2, resp.getOverdueTasks());
        assertEquals(50.0, resp.getOverdueRate());
    }

    @Test
    @DisplayName("7. Average session length is calculated correctly")
    void averageSessionLength_isCalculatedCorrectly() {
        // 2 completed sessions: 45 min and 75 min => avg = 60.0 min
        studySessionRepository.save(StudySession.builder().user(userA).startTime(nowTime.minusHours(4)).durationMinutes(45).isCompleted(true).build());
        studySessionRepository.save(StudySession.builder().user(userA).startTime(nowTime.minusHours(2)).durationMinutes(75).isCompleted(true).build());
        // 1 active uncompleted session => ignored in completed average
        studySessionRepository.save(StudySession.builder().user(userA).startTime(nowTime).durationMinutes(0).isCompleted(false).build());

        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(120, resp.getTotalStudyMinutes());
        assertEquals(2.0, resp.getTotalStudyHours());
        assertEquals(60.0, resp.getAverageSessionMinutes());
    }

    @Test
    @DisplayName("8. Total study minutes is calculated correctly across completed sessions")
    void totalStudyMinutes_isCalculatedCorrectly() {
        studySessionRepository.save(StudySession.builder().user(userA).startTime(nowTime.minusDays(1)).durationMinutes(90).isCompleted(true).build());
        studySessionRepository.save(StudySession.builder().user(userA).startTime(nowTime).durationMinutes(30).isCompleted(true).build());

        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(120, resp.getTotalStudyMinutes());
    }

    @Test
    @DisplayName("9 & 10. Weekly analytics returns 7 days in chronological order")
    void weeklyAnalytics_returnsSevenChronologicalDays() {
        WeeklyAnalyticsResponse resp = analyticsService.getWeeklyAnalytics(userA.getId());

        assertNotNull(resp);
        assertEquals(7, resp.getDailyPoints().size());
        assertEquals(todayDate.minusDays(6), resp.getStartDate());
        assertEquals(todayDate, resp.getEndDate());

        for (int i = 0; i < 7; i++) {
            LocalDate expectedDate = todayDate.minusDays(6 - i);
            assertEquals(expectedDate, resp.getDailyPoints().get(i).getDate());
        }
    }

    @Test
    @DisplayName("11. Weekly analytics uses actual database data")
    void weeklyAnalyticsUsesActualDatabaseData() {
        LocalDate yesterday = todayDate.minusDays(1);
        productivityStatRepository.save(ProductivityStat.builder()
                .user(userA)
                .statDate(yesterday)
                .tasksCompleted(2)
                .tasksOverdue(1)
                .studyMinutes(90)
                .productivityScore(new BigDecimal("78.50"))
                .build());

        WeeklyAnalyticsResponse resp = analyticsService.getWeeklyAnalytics(userA.getId());

        var yesterdayPoint = resp.getDailyPoints().stream()
                .filter(p -> p.getDate().equals(yesterday))
                .findFirst()
                .orElse(null);

        assertNotNull(yesterdayPoint);
        assertEquals(2, yesterdayPoint.getTasksCompleted());
        assertEquals(1, yesterdayPoint.getTasksOverdue());
        assertEquals(90, yesterdayPoint.getStudyMinutes());
        assertEquals(new BigDecimal("78.50"), yesterdayPoint.getProductivityScore());
    }

    @Test
    @DisplayName("12 & 13. Subject analytics aggregates tasks and study time correctly")
    void subjectAnalyticsAggregatesTasksAndStudyTimeCorrectly() {
        Task t1 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectCS)
                .title("Binary Tree Lab")
                .taskType(TaskType.LAB)
                .estimatedHours(new BigDecimal("3.5"))
                .status(TaskStatus.COMPLETED)
                .deadline(nowTime.plusDays(1))
                .build());

        Task t2 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectCS)
                .title("Dijkstra Algorithm Homework")
                .taskType(TaskType.ASSIGNMENT)
                .estimatedHours(new BigDecimal("2.5"))
                .status(TaskStatus.PENDING)
                .deadline(nowTime.plusDays(2))
                .build());

        // 60 minutes study on task 1
        studySessionRepository.save(StudySession.builder()
                .user(userA)
                .task(t1)
                .startTime(nowTime.minusHours(2))
                .durationMinutes(60)
                .isCompleted(true)
                .build());

        List<SubjectAnalyticsResponse> resp = analyticsService.getSubjectAnalytics(userA.getId());

        assertEquals(1, resp.size());
        SubjectAnalyticsResponse sub = resp.get(0);
        assertEquals(subjectCS.getId(), sub.getSubjectId());
        assertEquals(2L, sub.getTaskCount());
        assertEquals(1L, sub.getCompletedTaskCount());
        assertEquals(1L, sub.getPendingTaskCount());
        assertEquals(0L, sub.getOverdueTaskCount());
        assertEquals(6.0, sub.getEstimatedWorkloadHours());
        assertEquals(60, sub.getActualStudyMinutes());
        assertEquals(50.0, sub.getCompletionPercentage());
    }

    @Test
    @DisplayName("14. Unassigned tasks (subject == null) do not break subject analytics")
    void unassignedTasksDoNotBreakSubjectAnalytics() {
        taskRepository.save(Task.builder()
                .user(userA)
                .subject(null) // Unassigned
                .title("General Reading")
                .taskType(TaskType.READING)
                .status(TaskStatus.PENDING)
                .deadline(nowTime.plusDays(2))
                .build());

        List<SubjectAnalyticsResponse> resp = analyticsService.getSubjectAnalytics(userA.getId());
        assertEquals(1, resp.size()); // Only subjectCS is returned
    }

    @Test
    @DisplayName("15. User cannot see another user's analytics (Strict Tenant Isolation)")
    void userCannotSeeAnotherUsersAnalytics() {
        // Bob has 5 completed tasks and 180 study minutes
        taskRepository.save(Task.builder().user(userB).title("Bob Task 1").status(TaskStatus.COMPLETED).taskType(TaskType.ASSIGNMENT).deadline(nowTime).build());
        studySessionRepository.save(StudySession.builder().user(userB).startTime(nowTime).durationMinutes(180).isCompleted(true).build());

        AnalyticsDashboardResponse aliceDash = analyticsService.getDashboard(userA.getId());

        assertEquals(0, aliceDash.getTotalTasks());
        assertEquals(0, aliceDash.getTotalStudyMinutes());
    }

    @Test
    @DisplayName("16 & 17 & 18. Productivity stats are created, updated, and operation is idempotent")
    void productivityStats_areCreatedAndUpdatedIdempotently() {
        // First execution creates record for today
        ProductivityStat stat1 = analyticsService.generateOrUpdateDailyStats(userA.getId(), todayDate);
        assertNotNull(stat1.getId());
        assertEquals(todayDate, stat1.getStatDate());

        // Add 45 study minutes
        studySessionRepository.save(StudySession.builder()
                .user(userA)
                .startTime(nowTime)
                .durationMinutes(45)
                .isCompleted(true)
                .build());

        // Second execution updates existing row without duplicate constraint violation
        ProductivityStat stat2 = analyticsService.generateOrUpdateDailyStats(userA.getId(), todayDate);

        assertEquals(stat1.getId(), stat2.getId(), "Should update same row instead of inserting new one");
        assertEquals(45, stat2.getStudyMinutes());
        assertEquals(1, productivityStatRepository.count());
    }

    @Test
    @DisplayName("19. Zero task user returns zero-safe analytics without division by zero")
    void zeroTaskUser_returnsZeroSafeAnalytics() {
        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(0, resp.getTotalTasks());
        assertEquals(0.0, resp.getCompletionPercentage());
        assertEquals(0.0, resp.getOverdueRate());
    }

    @Test
    @DisplayName("20. Zero study session user returns zero average session length")
    void zeroStudySessionUser_returnsZeroAverageSession() {
        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(0, resp.getTotalStudyMinutes());
        assertEquals(0.0, resp.getAverageSessionMinutes());
    }

    @Test
    @DisplayName("21. Analytics uses injected Clock for deterministic testing")
    void analyticsUsesInjectedClock() {
        WeeklyAnalyticsResponse resp = analyticsService.getWeeklyAnalytics(userA.getId());
        assertEquals(todayDate, resp.getEndDate());
    }

    @Test
    @DisplayName("22. Analytics consumes existing Stage 6 priority records without recalculation")
    void analyticsConsumesExistingStage6Priority() {
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Critical Deliverable")
                .taskType(TaskType.PROJECT)
                .status(TaskStatus.IN_PROGRESS)
                .deadline(nowTime.plusDays(1))
                .build());

        taskPriorityRepository.save(TaskPriority.builder()
                .task(task)
                .priorityScore(new BigDecimal("85.00"))
                .priorityLevel(PriorityLevel.CRITICAL)
                .urgencyScore(BigDecimal.TEN)
                .difficultyScore(BigDecimal.TEN)
                .weightScore(BigDecimal.TEN)
                .effortScore(BigDecimal.TEN)
                .workloadScore(BigDecimal.TEN)
                .explanationText("Critical")
                .calculatedAt(nowTime)
                .build());

        AnalyticsDashboardResponse resp = analyticsService.getDashboard(userA.getId());

        assertEquals(1L, resp.getCriticalTaskCount());
    }
}
