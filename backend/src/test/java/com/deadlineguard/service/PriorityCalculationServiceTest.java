package com.deadlineguard.service;

import com.deadlineguard.dto.priority.PriorityResponse;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.PriorityLevel;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 6: Deterministic Priority Engine mathematical formula and service tests.
 * All tests use fixed, deterministic timestamps without relying on wall-clock time.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PriorityCalculationServiceTest {

    @Autowired
    private PriorityCalculationService priorityCalculationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    private User testUser;
    private final LocalDateTime referenceTime = LocalDateTime.of(2026, 10, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        taskPriorityRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .name("Alice Cooper")
                .email("alice.cooper@university.edu")
                .passwordHash("$2a$10$hashedpasswordstringforuser")
                .department("Computer Science")
                .semester(5)
                .college("Engineering College")
                .build());
    }

    @Test
    @DisplayName("1. Overdue task produces urgency score 100")
    void overdueTask_producesUrgencyScore100() {
        LocalDateTime pastDeadline = referenceTime.minusHours(5);
        BigDecimal score = priorityCalculationService.calculateUrgencyScore(pastDeadline, referenceTime);
        assertEquals(new BigDecimal("100.00"), score);

        // Also test exactly at referenceTime (0 hours remaining)
        BigDecimal exactScore = priorityCalculationService.calculateUrgencyScore(referenceTime, referenceTime);
        assertEquals(new BigDecimal("100.00"), exactScore);
    }

    @Test
    @DisplayName("2. Task within 168 hours follows linear urgency formula")
    void taskWithin168Hours_followsLinearUrgencyFormula() {
        // Due in 24 hours: 100 * (1 - 24/168) = 100 * (6/7) = 85.714... -> 85.71
        LocalDateTime in24Hours = referenceTime.plusHours(24);
        BigDecimal score24 = priorityCalculationService.calculateUrgencyScore(in24Hours, referenceTime);
        assertEquals(new BigDecimal("85.71"), score24);

        // Due in 72 hours: 100 * (1 - 72/168) = 100 * (96/168) = 57.142... -> 57.14
        LocalDateTime in72Hours = referenceTime.plusHours(72);
        BigDecimal score72 = priorityCalculationService.calculateUrgencyScore(in72Hours, referenceTime);
        assertEquals(new BigDecimal("57.14"), score72);

        // Due in exactly 168 hours: 100 * (1 - 168/168) = 0.00
        LocalDateTime in168Hours = referenceTime.plusHours(168);
        BigDecimal score168 = priorityCalculationService.calculateUrgencyScore(in168Hours, referenceTime);
        assertEquals(new BigDecimal("0.00"), score168);
    }

    @Test
    @DisplayName("3. Task beyond 168 hours follows existing long-term urgency formula")
    void taskBeyond168Hours_followsLongTermUrgencyFormula() {
        // Due in 168 + 252 = 420 hours: 20 * (1 - 252/504) = 20 * 0.5 = 10.00
        LocalDateTime in420Hours = referenceTime.plusHours(420);
        BigDecimal score420 = priorityCalculationService.calculateUrgencyScore(in420Hours, referenceTime);
        assertEquals(new BigDecimal("10.00"), score420);

        // Due in 168 + 504 = 672 hours (4 weeks): 20 * (1 - 504/504) = 0.00
        LocalDateTime in672Hours = referenceTime.plusHours(672);
        BigDecimal score672 = priorityCalculationService.calculateUrgencyScore(in672Hours, referenceTime);
        assertEquals(new BigDecimal("0.00"), score672);

        // Far in future: beyond 4 weeks clamp to 0.00
        LocalDateTime in1000Hours = referenceTime.plusHours(1000);
        BigDecimal score1000 = priorityCalculationService.calculateUrgencyScore(in1000Hours, referenceTime);
        assertEquals(new BigDecimal("0.00"), score1000);
    }

    @Test
    @DisplayName("4. Difficulty scoring is correct (1=0, 2=25, 3=50, 4=75, 5=100)")
    void difficultyScoring_isCorrect() {
        assertEquals(new BigDecimal("0.00"), priorityCalculationService.calculateDifficultyScore(1));
        assertEquals(new BigDecimal("25.00"), priorityCalculationService.calculateDifficultyScore(2));
        assertEquals(new BigDecimal("50.00"), priorityCalculationService.calculateDifficultyScore(3));
        assertEquals(new BigDecimal("75.00"), priorityCalculationService.calculateDifficultyScore(4));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateDifficultyScore(5));

        // Clamping edge cases
        assertEquals(new BigDecimal("0.00"), priorityCalculationService.calculateDifficultyScore(0));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateDifficultyScore(10));
    }

    @Test
    @DisplayName("5. Academic weight scoring is correct (weight * 2.5, clamped to 100)")
    void academicWeightScoring_isCorrect() {
        assertEquals(new BigDecimal("0.00"), priorityCalculationService.calculateAcademicWeightScore(BigDecimal.ZERO));
        assertEquals(new BigDecimal("25.00"), priorityCalculationService.calculateAcademicWeightScore(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("50.00"), priorityCalculationService.calculateAcademicWeightScore(new BigDecimal("20.00")));
        assertEquals(new BigDecimal("75.00"), priorityCalculationService.calculateAcademicWeightScore(new BigDecimal("30.00")));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateAcademicWeightScore(new BigDecimal("40.00")));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateAcademicWeightScore(new BigDecimal("60.00")));
    }

    @Test
    @DisplayName("6. Effort scoring is correct ((hours / 10) * 100, clamped to 100)")
    void effortScoring_isCorrect() {
        assertEquals(new BigDecimal("10.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("1.0")));
        assertEquals(new BigDecimal("25.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("2.5")));
        assertEquals(new BigDecimal("50.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("5.0")));
        assertEquals(new BigDecimal("80.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("8.0")));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("10.0")));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateEffortScore(new BigDecimal("15.0")));
    }

    @Test
    @DisplayName("7. Workload scoring is correct (concurrentCount * 20, clamped to 100)")
    void workloadScoring_isCorrect() {
        assertEquals(new BigDecimal("0.00"), priorityCalculationService.calculateWorkloadScore(0));
        assertEquals(new BigDecimal("20.00"), priorityCalculationService.calculateWorkloadScore(1));
        assertEquals(new BigDecimal("40.00"), priorityCalculationService.calculateWorkloadScore(2));
        assertEquals(new BigDecimal("60.00"), priorityCalculationService.calculateWorkloadScore(3));
        assertEquals(new BigDecimal("80.00"), priorityCalculationService.calculateWorkloadScore(4));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateWorkloadScore(5));
        assertEquals(new BigDecimal("100.00"), priorityCalculationService.calculateWorkloadScore(9));
    }

    @Test
    @DisplayName("8. Weighted composite score is correct (0.4U + 0.2D + 0.2W + 0.1E + 0.1L)")
    void weightedCompositeScore_isCorrect() {
        // U=100 (0.4*100=40), D=75 (0.2*75=15), W=50 (0.2*50=10), E=60 (0.1*60=6), L=40 (0.1*40=4)
        // Composite = 40 + 15 + 10 + 6 + 4 = 75.00
        BigDecimal composite = priorityCalculationService.calculateCompositeScore(
                new BigDecimal("100.00"),
                new BigDecimal("75.00"),
                new BigDecimal("50.00"),
                new BigDecimal("60.00"),
                new BigDecimal("40.00")
        );
        assertEquals(new BigDecimal("75.00"), composite);
    }

    @Test
    @DisplayName("9. Priority tier classification is correct for exact boundary values")
    void priorityTier_isCorrectForBoundaries() {
        // Tier boundaries:
        // 75.99 -> HIGH
        // 76.00 -> CRITICAL
        assertEquals(PriorityLevel.HIGH, priorityCalculationService.determinePriorityTier(new BigDecimal("75.99")));
        assertEquals(PriorityLevel.CRITICAL, priorityCalculationService.determinePriorityTier(new BigDecimal("76.00")));
        assertEquals(PriorityLevel.CRITICAL, priorityCalculationService.determinePriorityTier(new BigDecimal("100.00")));

        // 50.99 -> MEDIUM
        // 51.00 -> HIGH
        assertEquals(PriorityLevel.MEDIUM, priorityCalculationService.determinePriorityTier(new BigDecimal("50.99")));
        assertEquals(PriorityLevel.HIGH, priorityCalculationService.determinePriorityTier(new BigDecimal("51.00")));

        // 30.99 -> LOW
        // 31.00 -> MEDIUM
        assertEquals(PriorityLevel.LOW, priorityCalculationService.determinePriorityTier(new BigDecimal("30.99")));
        assertEquals(PriorityLevel.MEDIUM, priorityCalculationService.determinePriorityTier(new BigDecimal("31.00")));
        assertEquals(PriorityLevel.LOW, priorityCalculationService.determinePriorityTier(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("10. Priority is persisted to TaskPriority entity")
    void priority_isPersistedToTaskPriority() {
        Task task = taskRepository.save(Task.builder()
                .user(testUser)
                .title("Operating Systems Project")
                .taskType(TaskType.PROJECT)
                .deadline(referenceTime.plusHours(48))
                .difficulty(4)
                .academicWeight(new BigDecimal("25.00"))
                .estimatedHours(new BigDecimal("8.0"))
                .status(TaskStatus.PENDING)
                .build());

        PriorityResponse response = priorityCalculationService.calculateAndPersistPriority(task.getId(), testUser.getId(), referenceTime);

        assertNotNull(response);
        assertNotNull(response.getPriorityScore());
        assertEquals(task.getId(), response.getTaskId());

        // Verify direct entity persistence in repository
        TaskPriority persisted = taskPriorityRepository.findByTaskId(task.getId()).orElse(null);
        assertNotNull(persisted);
        assertEquals(response.getPriorityScore(), persisted.getPriorityScore());
        assertEquals(response.getPriorityTier(), persisted.getPriorityLevel());
        assertNotNull(persisted.getExplanationText());
    }

    @Test
    @DisplayName("11. Recalculation updates an existing TaskPriority record without creating duplicates")
    void recalculation_updatesExistingTaskPriority() {
        Task task = taskRepository.save(Task.builder()
                .user(testUser)
                .title("Database Homework")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(referenceTime.plusHours(120))
                .difficulty(2)
                .academicWeight(new BigDecimal("10.00"))
                .estimatedHours(new BigDecimal("2.0"))
                .status(TaskStatus.PENDING)
                .build());

        // First calculation
        PriorityResponse firstRes = priorityCalculationService.calculateAndPersistPriority(task.getId(), testUser.getId(), referenceTime);
        long initialCount = taskPriorityRepository.count();

        // Update task difficulty and recalculate
        task.setDifficulty(5);
        taskRepository.save(task);

        PriorityResponse updatedRes = priorityCalculationService.calculateAndPersistPriority(task.getId(), testUser.getId(), referenceTime);

        // Record count must remain 1 (no duplicate TaskPriority rows for same task)
        assertEquals(initialCount, taskPriorityRepository.count());
        assertTrue(updatedRes.getPriorityScore().compareTo(firstRes.getPriorityScore()) > 0);
        assertEquals(firstRes.getId(), updatedRes.getId());
    }
}
