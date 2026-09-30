package com.deadlineguard.service;

import com.deadlineguard.dto.analytics.*;
import com.deadlineguard.entity.ProductivityStat;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing student productivity analytics, dashboard metrics,
 * 7-day chronological trends, and subject-level academic workload breakdown.
 *
 * ARCHITECTURAL CONTRACT:
 * - Deterministic Metric Definitions: All rates, scores, and percentages use explicit formulas.
 * - Idempotent Productivity Stats: Generates or updates (user_id, stat_date) without duplicates.
 * - Authoritative Stage 6 Priority: Reads existing TaskPriority records without recalculation.
 * - Multi-tenant Isolation: All calculations and queries are strictly scoped to the authenticated student.
 * - Injected Clock: All temporal operations use java.time.Clock for deterministic testing in UTC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubjectRepository subjectRepository;
    private final StudySessionRepository studySessionRepository;
    private final ProductivityStatRepository productivityStatRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final Clock clock;

    // =========================================================================
    // 1. DASHBOARD ANALYTICS OVERVIEW
    // =========================================================================

    /**
     * Compute and return the student's current productivity overview.
     */
    @Transactional
    public AnalyticsDashboardResponse getDashboard(Long userId) {
        log.info("Generating analytics dashboard for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        List<Task> allTasks = taskRepository.findByUserId(userId);
        long totalTasks = allTasks.size();

        long completedTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();

        long pendingTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .count();

        long activeTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        long overdueTasks = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null && t.getDeadline().isBefore(now))
                .count();

        double completionPercentage = totalTasks == 0 ? 0.0
                : roundToTwoDecimals(((double) completedTasks / totalTasks) * 100.0);

        double overdueRate = totalTasks == 0 ? 0.0
                : roundToTwoDecimals(((double) overdueTasks / totalTasks) * 100.0);

        int totalStudyMinutes = studySessionRepository.sumTotalStudyMinutesForUser(userId);
        double totalStudyHours = roundToTwoDecimals(totalStudyMinutes / 60.0);

        long completedSessionCount = studySessionRepository.countByUserIdAndIsCompletedTrue(userId);
        double averageSessionMinutes = completedSessionCount == 0 ? 0.0
                : roundToTwoDecimals((double) totalStudyMinutes / completedSessionCount);

        // Critical tasks count from Stage 6 records
        long criticalCount = allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .map(t -> taskPriorityRepository.findByTaskId(t.getId()).orElse(null))
                .filter(p -> p != null && (p.getPriorityLevel() == PriorityLevel.CRITICAL
                        || (p.getPriorityScore() != null && p.getPriorityScore().compareTo(new BigDecimal("76.00")) >= 0)))
                .count();

        // 7-day weekly metrics
        LocalDateTime startOf7Days = today.minusDays(6).atStartOfDay();
        int weeklyStudyMinutes = studySessionRepository.sumDurationMinutesForPeriod(userId, startOf7Days, now);

        int weeklyCompletedTasks = (int) allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCreatedAt() != null && !t.getCreatedAt().isBefore(startOf7Days))
                .count();

        int weeklyOverdueTasks = (int) allTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null
                        && t.getDeadline().isBefore(now) && !t.getDeadline().isBefore(startOf7Days))
                .count();

        // Ensure today's productivity_stats is up to date and retrieve current score
        ProductivityStat todayStat = generateOrUpdateDailyStats(userId, today);
        BigDecimal currentScore = todayStat.getProductivityScore();

        List<SubjectAnalyticsResponse> subjectSummaries = getSubjectAnalytics(userId);

        return AnalyticsDashboardResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .completionPercentage(completionPercentage)
                .overdueRate(overdueRate)
                .totalStudyMinutes(totalStudyMinutes)
                .totalStudyHours(totalStudyHours)
                .averageSessionMinutes(averageSessionMinutes)
                .currentProductivityScore(currentScore)
                .weeklyStudyMinutes(weeklyStudyMinutes)
                .weeklyCompletedTasks(weeklyCompletedTasks)
                .weeklyOverdueTasks(weeklyOverdueTasks)
                .activeTaskCount(activeTasks)
                .criticalTaskCount(criticalCount)
                .subjects(subjectSummaries)
                .build();
    }

    // =========================================================================
    // 2. WEEKLY 7-DAY PRODUCTIVITY TRENDS
    // =========================================================================

    /**
     * Return chronological daily metrics over the trailing 7-day window.
     * Guarantees 7 points in chronological order.
     */
    @Transactional
    public WeeklyAnalyticsResponse getWeeklyAnalytics(Long userId) {
        log.info("Generating weekly analytics for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(6);

        Map<LocalDate, ProductivityStat> existingMap = productivityStatRepository
                .findByUserIdAndStatDateBetweenOrderByStatDateAsc(userId, startDate, today)
                .stream()
                .collect(Collectors.toMap(ProductivityStat::getStatDate, s -> s));

        List<DailyAnalyticsPoint> dailyPoints = new ArrayList<>();
        int totalWeeklyStudy = 0;
        int totalWeeklyCompleted = 0;
        int totalWeeklyOverdue = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            ProductivityStat stat = existingMap.get(date);

            if (stat == null) {
                // If it's today, dynamically compute and persist
                if (date.equals(today)) {
                    stat = generateOrUpdateDailyStats(userId, today);
                } else {
                    // Compute historical day data from database
                    stat = calculateDailyStatsFromData(user, date);
                }
            }

            dailyPoints.add(DailyAnalyticsPoint.builder()
                    .date(date)
                    .tasksCompleted(stat.getTasksCompleted())
                    .tasksOverdue(stat.getTasksOverdue())
                    .studyMinutes(stat.getStudyMinutes())
                    .productivityScore(stat.getProductivityScore())
                    .build());

            totalWeeklyStudy += stat.getStudyMinutes();
            totalWeeklyCompleted += stat.getTasksCompleted();
            totalWeeklyOverdue += stat.getTasksOverdue();
            scoreSum = scoreSum.add(stat.getProductivityScore());
        }

        BigDecimal averageScore = scoreSum.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);

        return WeeklyAnalyticsResponse.builder()
                .startDate(startDate)
                .endDate(today)
                .totalWeeklyStudyMinutes(totalWeeklyStudy)
                .totalWeeklyTasksCompleted(totalWeeklyCompleted)
                .totalWeeklyTasksOverdue(totalWeeklyOverdue)
                .averageProductivityScore(averageScore)
                .dailyPoints(dailyPoints)
                .build();
    }

    // =========================================================================
    // 3. SUBJECT-WISE WORKLOAD & PRODUCTIVITY
    // =========================================================================

    /**
     * Return subject-level academic workload, estimated hours, and completed study minutes.
     */
    @Transactional(readOnly = true)
    public List<SubjectAnalyticsResponse> getSubjectAnalytics(Long userId) {
        log.info("Generating subject analytics for userId: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDateTime now = LocalDateTime.now(clock);
        List<Subject> subjects = subjectRepository.findByUserId(userId);
        List<SubjectAnalyticsResponse> responses = new ArrayList<>();

        for (Subject subject : subjects) {
            List<Task> subjectTasks = taskRepository.findByUserIdAndSubjectId(userId, subject.getId());
            long taskCount = subjectTasks.size();

            long completedCount = subjectTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .count();

            long pendingCount = subjectTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.PENDING)
                    .count();

            long overdueCount = subjectTasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.COMPLETED && t.getDeadline() != null && t.getDeadline().isBefore(now))
                    .count();

            double estimatedWorkloadHours = subjectTasks.stream()
                    .map(Task::getEstimatedHours)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            int actualStudyMinutes = studySessionRepository.sumStudyMinutesBySubjectId(userId, subject.getId());

            double completionPercentage = taskCount == 0 ? 0.0
                    : roundToTwoDecimals(((double) completedCount / taskCount) * 100.0);

            responses.add(SubjectAnalyticsResponse.builder()
                    .subjectId(subject.getId())
                    .subjectName(subject.getSubjectName())
                    .subjectCode(subject.getSubjectCode())
                    .colorHex(subject.getColorHex())
                    .taskCount(taskCount)
                    .completedTaskCount(completedCount)
                    .pendingTaskCount(pendingCount)
                    .overdueTaskCount(overdueCount)
                    .estimatedWorkloadHours(roundToTwoDecimals(estimatedWorkloadHours))
                    .actualStudyMinutes(actualStudyMinutes)
                    .completionPercentage(completionPercentage)
                    .build());
        }

        return responses;
    }

    // =========================================================================
    // 4. IDEMPOTENT DAILY PRODUCTIVITY STATS PERSISTENCE & COMPUTATION
    // =========================================================================

    /**
     * Calculate and persist/update daily productivity stat record for a student.
     * Enforces (user_id, stat_date) uniqueness and idempotency.
     */
    @Transactional
    public ProductivityStat generateOrUpdateDailyStats(Long userId, LocalDate statDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ProductivityStat computed = calculateDailyStatsFromData(user, statDate);

        Optional<ProductivityStat> existing = productivityStatRepository.findByUserIdAndStatDate(userId, statDate);
        if (existing.isPresent()) {
            ProductivityStat stat = existing.get();
            stat.setTasksCompleted(computed.getTasksCompleted());
            stat.setTasksOverdue(computed.getTasksOverdue());
            stat.setStudyMinutes(computed.getStudyMinutes());
            stat.setProductivityScore(computed.getProductivityScore());
            return productivityStatRepository.save(stat);
        } else {
            return productivityStatRepository.save(computed);
        }
    }

    /**
     * Compute productivity metric point from raw tasks and focus sessions without persistence.
     */
    private ProductivityStat calculateDailyStatsFromData(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Task> allUserTasks = taskRepository.findByUserId(user.getId());

        // Tasks completed by this date
        int completedCount = (int) allUserTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED
                        && t.getCreatedAt() != null && !t.getCreatedAt().isAfter(endOfDay))
                .count();

        // Tasks overdue on this date
        int overdueCount = (int) allUserTasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED
                        && t.getDeadline() != null && t.getDeadline().isBefore(endOfDay)
                        && t.getDeadline().isAfter(startOfDay.minusDays(30))) // within reasonable overdue horizon
                .count();

        int studyMinutes = studySessionRepository.sumDurationMinutesForPeriod(user.getId(), startOfDay, endOfDay);

        int totalDayTasks = completedCount + overdueCount;
        BigDecimal score = calculateProductivityScore(completedCount, overdueCount, totalDayTasks, studyMinutes);

        return ProductivityStat.builder()
                .user(user)
                .statDate(date)
                .tasksCompleted(completedCount)
                .tasksOverdue(overdueCount)
                .studyMinutes(studyMinutes)
                .productivityScore(score)
                .build();
    }

    /**
     * Deterministic, explainable daily productivity score calculation:
     *
     * completionRatio = (tasksCompleted / totalTasksForThatDay) * 100
     * overduePenalty  = (tasksOverdue / totalTasksForThatDay) * 100
     * studyComponent  = min(100, (studyMinutes / 120.0) * 100)  [2 hours study = 100 points]
     *
     * dailyScore = 0.50 * completionRatio + 0.20 * (100 - overduePenalty) + 0.30 * studyComponent
     * Clamped strictly to 0.00 - 100.00.
     */
    public BigDecimal calculateProductivityScore(int tasksCompleted, int tasksOverdue, int totalTasksForDay, int studyMinutes) {
        double completionRatio = totalTasksForDay > 0
                ? ((double) tasksCompleted / totalTasksForDay) * 100.0
                : (tasksCompleted > 0 ? 100.0 : 0.0);

        double overduePenalty = totalTasksForDay > 0
                ? ((double) tasksOverdue / totalTasksForDay) * 100.0
                : 0.0;

        double studyComponent = Math.min(100.0, (studyMinutes / 120.0) * 100.0);

        double rawScore;
        if (totalTasksForDay == 0 && tasksCompleted == 0 && tasksOverdue == 0) {
            // No deliverables due on this date: score is purely study focus driven
            rawScore = studyComponent;
        } else {
            rawScore = (0.50 * completionRatio)
                    + (0.20 * Math.max(0.0, 100.0 - overduePenalty))
                    + (0.30 * studyComponent);
        }

        double clamped = Math.max(0.0, Math.min(100.0, rawScore));
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }

    private double roundToTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
