package com.deadlineguard.service;

import com.deadlineguard.dto.priority.PriorityResponse;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deterministic Explainable Task Priority Engine.
 *
 * Implements the frozen 5-factor priority formula:
 *   PriorityScore = 0.40 * UrgencyScore
 *                 + 0.20 * DifficultyScore
 *                 + 0.20 * AcademicWeightScore
 *                 + 0.10 * EffortScore
 *                 + 0.10 * WorkloadScore
 *
 * Zero external AI / LLM dependencies. Fully reproducible and mathematically bounded in [0.00, 100.00].
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriorityCalculationService {

    private final TaskRepository taskRepository;
    private final TaskPriorityRepository taskPriorityRepository;

    /**
     * Factor 1: Urgency Score (U in [0.00, 100.00])
     *
     * Rules:
     * A. If overdue: Urgency = 100.00
     * B. If within 168 hours (7 days): Decreases linearly from 100 to 0 over 168 hours:
     *    U = 100.00 * (1.0 - (hoursRemaining / 168.0))
     * C. If beyond 168 hours: Decreases gently from 20 to 0 over next 504 hours (weeks 2 to 4):
     *    U = max(0.00, 20.00 * (1.0 - ((hoursRemaining - 168.0) / 504.0)))
     */
    public BigDecimal calculateUrgencyScore(LocalDateTime deadline, LocalDateTime now) {
        if (deadline == null || !deadline.isAfter(now)) {
            return BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        }

        double seconds = Duration.between(now, deadline).toSeconds();
        double hours = seconds / 3600.0;

        if (hours <= 0.0) {
            return BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        }

        double score;
        if (hours <= 168.0) {
            score = 100.00 * (1.0 - (hours / 168.0));
        } else {
            score = Math.max(0.00, 20.00 * (1.0 - ((hours - 168.0) / 504.0)));
        }

        double clamped = Math.max(0.00, Math.min(100.00, score));
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Factor 2: Difficulty Score (D in [0.00, 100.00])
     * Linear mapping for difficulty rating 1–5:
     *   D = ((difficulty - 1) / 4.0) * 100.00
     */
    public BigDecimal calculateDifficultyScore(Integer difficulty) {
        if (difficulty == null) {
            difficulty = 3;
        }
        int clamped = Math.max(1, Math.min(5, difficulty));
        double score = ((clamped - 1) / 4.0) * 100.00;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Factor 3: Academic Weight Score (W in [0.00, 100.00])
     * Scales course percentage weight:
     *   W = min(100.00, academicWeight * 2.50)
     */
    public BigDecimal calculateAcademicWeightScore(BigDecimal academicWeight) {
        if (academicWeight == null) {
            academicWeight = BigDecimal.ZERO;
        }
        double weight = academicWeight.doubleValue();
        double clampedWeight = Math.max(0.00, Math.min(100.00, weight));
        double score = Math.min(100.00, clampedWeight * 2.50);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Factor 4: Effort Score (E in [0.00, 100.00])
     * Normalizes estimated hours relative to a 10.0-hour standard ceiling:
     *   E = min(100.00, (estimatedHours / 10.0) * 100.00)
     */
    public BigDecimal calculateEffortScore(BigDecimal estimatedHours) {
        if (estimatedHours == null) {
            estimatedHours = BigDecimal.ONE;
        }
        double hours = estimatedHours.doubleValue();
        double clampedHours = Math.max(0.00, hours);
        double score = Math.min(100.00, (clampedHours / 10.0) * 100.00);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Factor 5: Concurrent Workload Score (L in [0.00, 100.00])
     * Evaluates competition from other incomplete tasks in a 72-hour window (+/- 36h):
     *   L = min(100.00, concurrentTaskCount * 20.00)
     */
    public BigDecimal calculateWorkloadScore(long concurrentTaskCount) {
        long count = Math.max(0L, concurrentTaskCount);
        double score = Math.min(100.00, count * 20.00);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Weighted Composite Priority Score
     *   Score = 0.40 * U + 0.20 * D + 0.20 * W + 0.10 * E + 0.10 * L
     * Bounded strictly in [0.00, 100.00], rounded to 2 decimal places.
     */
    public BigDecimal calculateCompositeScore(
            BigDecimal urgency,
            BigDecimal difficulty,
            BigDecimal weight,
            BigDecimal effort,
            BigDecimal workload
    ) {
        double composite = 0.40 * urgency.doubleValue()
                         + 0.20 * difficulty.doubleValue()
                         + 0.20 * weight.doubleValue()
                         + 0.10 * effort.doubleValue()
                         + 0.10 * workload.doubleValue();
        double clamped = Math.max(0.00, Math.min(100.00, composite));
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Priority Level Categorical Classification:
     *   76.00 – 100.00 -> CRITICAL
     *   51.00 –  75.99 -> HIGH
     *   31.00 –  50.99 -> MEDIUM
     *    0.00 –  30.99 -> LOW
     */
    public PriorityLevel determinePriorityTier(BigDecimal compositeScore) {
        double score = compositeScore.doubleValue();
        if (score >= 76.00) {
            return PriorityLevel.CRITICAL;
        } else if (score >= 51.00) {
            return PriorityLevel.HIGH;
        } else if (score >= 31.00) {
            return PriorityLevel.MEDIUM;
        } else {
            return PriorityLevel.LOW;
        }
    }

    /**
     * Generate clear, explainable breakdown text for student transparency.
     */
    public String generateExplanation(
            BigDecimal compositeScore,
            PriorityLevel tier,
            BigDecimal urgency,
            BigDecimal difficulty,
            BigDecimal weight,
            BigDecimal effort,
            BigDecimal workload
    ) {
        return String.format(
            "Priority Score: %.2f (%s). 5-Factor Breakdown: Urgency: %.2f (40%% weight), Difficulty: %.2f (20%% weight), Academic Weight: %.2f (20%% weight), Effort: %.2f (10%% weight), Concurrent Workload: %.2f (10%% weight).",
            compositeScore, tier, urgency, difficulty, weight, effort, workload
        );
    }

    /**
     * Calculate and persist priority for a task with deterministic reference time.
     * Enforces student task ownership.
     */
    @Transactional
    public PriorityResponse calculateAndPersistPriority(Long taskId, Long userId, LocalDateTime referenceTime) {
        log.info("Calculating priority for taskId: {}, userId: {} at referenceTime: {}", taskId, userId, referenceTime);

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Concurrent workload: active tasks in 72h window centered at task deadline (+/- 36h)
        LocalDateTime windowStart = task.getDeadline().minusHours(36);
        LocalDateTime windowEnd = task.getDeadline().plusHours(36);
        long concurrentCount = taskRepository.countConcurrentWorkloadTasks(userId, task.getId(), windowStart, windowEnd);

        BigDecimal urgency = calculateUrgencyScore(task.getDeadline(), referenceTime);
        BigDecimal difficulty = calculateDifficultyScore(task.getDifficulty());
        BigDecimal weight = calculateAcademicWeightScore(task.getAcademicWeight());
        BigDecimal effort = calculateEffortScore(task.getEstimatedHours());
        BigDecimal workload = calculateWorkloadScore(concurrentCount);

        BigDecimal composite = calculateCompositeScore(urgency, difficulty, weight, effort, workload);
        PriorityLevel tier = determinePriorityTier(composite);
        String explanation = generateExplanation(composite, tier, urgency, difficulty, weight, effort, workload);

        TaskPriority priority = taskPriorityRepository.findByTaskId(taskId)
                .orElseGet(() -> TaskPriority.builder().task(task).build());

        priority.setPriorityScore(composite);
        priority.setPriorityLevel(tier);
        priority.setUrgencyScore(urgency);
        priority.setDifficultyScore(difficulty);
        priority.setWeightScore(weight);
        priority.setEffortScore(effort);
        priority.setWorkloadScore(workload);
        priority.setExplanationText(explanation);
        priority.setCalculatedAt(referenceTime);

        TaskPriority saved = taskPriorityRepository.save(priority);
        return PriorityResponse.fromEntity(saved);
    }

    /**
     * Calculate and persist priority using current wall-clock time.
     */
    @Transactional
    public PriorityResponse calculateAndPersistPriority(Long taskId, Long userId) {
        return calculateAndPersistPriority(taskId, userId, LocalDateTime.now());
    }

    /**
     * Retrieve current priority for an owned task. If not yet calculated, calculates on the fly.
     */
    @Transactional
    public PriorityResponse getTaskPriority(Long taskId, Long userId) {
        // Enforce ownership: will throw 404 if task does not belong to user
        taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        return taskPriorityRepository.findByTaskIdAndTaskUserId(taskId, userId)
                .map(PriorityResponse::fromEntity)
                .orElseGet(() -> calculateAndPersistPriority(taskId, userId));
    }

    /**
     * Retrieve all task priorities belonging to the authenticated student,
     * sorted by score DESC or deadline ASC.
     */
    @Transactional(readOnly = true)
    public List<PriorityResponse> getUserTaskPriorities(Long userId, String sortBy) {
        List<TaskPriority> priorities;
        if ("deadline".equalsIgnoreCase(sortBy)) {
            priorities = taskPriorityRepository.findByTaskUserIdOrderByTaskDeadlineAsc(userId);
        } else {
            priorities = taskPriorityRepository.findByTaskUserIdOrderByPriorityScoreDesc(userId);
        }

        return priorities.stream()
                .map(PriorityResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
