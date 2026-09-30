package com.deadlineguard.service;

import com.deadlineguard.dto.study.GenerateStudyPlanRequest;
import com.deadlineguard.dto.study.StudyPlanResponse;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.exception.BadRequestException;
import com.deadlineguard.repository.AIRecommendationRepository;
import com.deadlineguard.repository.SubjectRepository;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.service.ai.GeminiClient;
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
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Stage 8: StudyPlanService unit & integration tests with deterministic mock Gemini provider.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyPlanServiceTest {

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    @Autowired
    private AIRecommendationRepository aiRecommendationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private GeminiClient geminiClient;

    private User userA;
    private User userB;
    private Subject subjectA;
    private Task taskA1;
    private Task taskA2;
    private Task taskB1;
    private final LocalDateTime referenceStartTime = LocalDateTime.of(2026, 10, 1, 18, 0, 0);

    @BeforeEach
    void setUp() {
        aiRecommendationRepository.deleteAll();
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

        subjectA = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Computer Systems")
                .subjectCode("CS301")
                .credits(4)
                .colorHex("#3B82F6")
                .build());

        // Low priority task (easy, 10 days out)
        taskA1 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("CS301 Reading Chapter 1")
                .taskType(TaskType.READING)
                .deadline(referenceStartTime.plusDays(10))
                .difficulty(1)
                .academicWeight(new BigDecimal("5.00"))
                .estimatedHours(new BigDecimal("1.0"))
                .status(TaskStatus.PENDING)
                .build());

        // High priority task (hard, due tomorrow)
        taskA2 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Memory Allocator Project")
                .taskType(TaskType.PROJECT)
                .deadline(referenceStartTime.plusHours(24))
                .difficulty(5)
                .academicWeight(new BigDecimal("30.00"))
                .estimatedHours(new BigDecimal("6.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build());

        // Bob's task (foreign user)
        taskB1 = taskRepository.save(Task.builder()
                .user(userB)
                .title("Fluid Dynamics Problem Set")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(referenceStartTime.plusHours(48))
                .difficulty(4)
                .academicWeight(new BigDecimal("20.00"))
                .estimatedHours(new BigDecimal("4.0"))
                .status(TaskStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("1. Authenticated user can generate a study plan")
    void authenticatedUser_canGenerateStudyPlan() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        String mockPlanJson = String.format("""
                {
                  "planDate": "2026-10-01",
                  "sessions": [
                    {
                      "taskId": %d,
                      "title": "Memory Allocator Project",
                      "subjectCode": "CS301",
                      "startTime": "2026-10-01T18:00:00",
                      "endTime": "2026-10-01T19:30:00",
                      "durationMinutes": 90,
                      "reason": "Highest priority deliverable due in 24 hours"
                    }
                  ],
                  "totalStudyMinutes": 90,
                  "totalBreakMinutes": 0
                }
                """, taskA2.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(mockPlanJson);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(3.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(15)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertNotNull(response);
        assertFalse(response.getFallbackUsed());
        assertEquals(1, response.getSessions().size());
        assertEquals(taskA2.getId(), response.getSessions().get(0).getTaskId());
        assertEquals(90, response.getTotalStudyMinutes());
        assertNotNull(response.getRecommendationId());
    }

    @Test
    @DisplayName("2. Plan uses only authenticated user's tasks")
    void planUsesOnlyAuthenticatedUsersTasks() {
        when(geminiClient.isConfigured()).thenReturn(false); // test deterministic fallback

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(3.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(15)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        // Verify none of the blocks belong to user B
        for (var session : response.getSessions()) {
            assertNotEquals(taskB1.getId(), session.getTaskId());
        }
    }

    @Test
    @DisplayName("3. Plan uses Stage 6 priority data (schedules higher priority task first)")
    void planUsesStage6PriorityData() {
        when(geminiClient.isConfigured()).thenReturn(false);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(3.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(15)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertNotNull(response);
        assertFalse(response.getSessions().isEmpty());
        // taskA2 has higher priority than taskA1, so it must be the first scheduled block
        assertEquals(taskA2.getId(), response.getSessions().get(0).getTaskId());
    }

    @Test
    @DisplayName("4. Invalid availableHours returns 400")
    void invalidAvailableHours_returns400() {
        GenerateStudyPlanRequest zeroHours = GenerateStudyPlanRequest.builder()
                .availableHours(0.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(15)
                .build();

        assertThrows(BadRequestException.class, () -> studyPlanService.generateStudyPlan(userA.getId(), zeroHours));

        GenerateStudyPlanRequest overMaxHours = GenerateStudyPlanRequest.builder()
                .availableHours(25.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(15)
                .build();

        assertThrows(BadRequestException.class, () -> studyPlanService.generateStudyPlan(userA.getId(), overMaxHours));
    }

    @Test
    @DisplayName("5. Invalid break duration returns 400")
    void invalidBreakDuration_returns400() {
        GenerateStudyPlanRequest negativeBreak = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(-5)
                .build();

        assertThrows(BadRequestException.class, () -> studyPlanService.generateStudyPlan(userA.getId(), negativeBreak));
    }

    @Test
    @DisplayName("6. Invalid start time returns 400")
    void invalidStartTime_returns400() {
        GenerateStudyPlanRequest nullStartTime = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(null)
                .breakDurationMinutes(15)
                .build();

        assertThrows(BadRequestException.class, () -> studyPlanService.generateStudyPlan(userA.getId(), nullStartTime));
    }

    @Test
    @DisplayName("7. Gemini structured response is parsed correctly")
    void geminiStructuredResponseIsParsed() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        String mockPlanJson = String.format("""
                {
                  "planDate": "2026-10-01",
                  "sessions": [
                    {
                      "taskId": %d,
                      "title": "Allocator Project",
                      "startTime": "2026-10-01T18:00:00",
                      "endTime": "2026-10-01T19:00:00",
                      "durationMinutes": 60,
                      "reason": "Top priority deliverable"
                    }
                  ],
                  "totalStudyMinutes": 60,
                  "totalBreakMinutes": 0
                }
                """, taskA2.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(mockPlanJson);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertFalse(response.getFallbackUsed());
        assertEquals("Allocator Project", response.getSessions().get(0).getTitle());
    }

    @Test
    @DisplayName("8. Invalid Gemini JSON triggers fallback")
    void invalidGeminiJsonTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn("NOT_VALID_JSON");

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed());
        assertFalse(response.getSessions().isEmpty());
    }

    @Test
    @DisplayName("9. Gemini timeout triggers fallback")
    void geminiTimeoutTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenThrow(new TimeoutException("Gemini call timed out"));

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed());
    }

    @Test
    @DisplayName("10. Missing API key triggers fallback")
    void missingApiKeyTriggersFallback() {
        when(geminiClient.isConfigured()).thenReturn(false);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed());
    }

    @Test
    @DisplayName("11. Gemini foreign task ID triggers fallback")
    void geminiForeignTaskIdTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        // Gemini returns Bob's task ID (taskB1) for Alice
        String foreignTaskJson = String.format("""
                {
                  "planDate": "2026-10-01",
                  "sessions": [
                    {
                      "taskId": %d,
                      "title": "Fluid Dynamics",
                      "startTime": "2026-10-01T18:00:00",
                      "endTime": "2026-10-01T19:00:00",
                      "durationMinutes": 60,
                      "reason": "Attempting cross tenant access"
                    }
                  ],
                  "totalStudyMinutes": 60,
                  "totalBreakMinutes": 0
                }
                """, taskB1.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(foreignTaskJson);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed(), "Foreign task ID must trigger fallback");
        assertEquals(taskA2.getId(), response.getSessions().get(0).getTaskId());
    }

    @Test
    @DisplayName("12. Overlapping plan blocks are rejected and trigger fallback")
    void overlappingPlanBlocksAreRejected() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        // Session 2 starts at 18:30 while Session 1 ends at 19:00 (overlap!)
        String overlappingJson = String.format("""
                {
                  "planDate": "2026-10-01",
                  "sessions": [
                    {
                      "taskId": %d,
                      "title": "Task 1",
                      "startTime": "2026-10-01T18:00:00",
                      "endTime": "2026-10-01T19:00:00",
                      "durationMinutes": 60,
                      "reason": "Session 1"
                    },
                    {
                      "taskId": %d,
                      "title": "Task 2",
                      "startTime": "2026-10-01T18:30:00",
                      "endTime": "2026-10-01T19:30:00",
                      "durationMinutes": 60,
                      "reason": "Overlapping session"
                    }
                  ],
                  "totalStudyMinutes": 120,
                  "totalBreakMinutes": 0
                }
                """, taskA2.getId(), taskA1.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(overlappingJson);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(3.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed(), "Overlapping blocks must trigger fallback");
    }

    @Test
    @DisplayName("13. Plan cannot exceed requested available time (triggers fallback if exceeded)")
    void planCannotExceedRequestedAvailableTime() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        // Student requested 1.0 hour (60 min), but model returned 120 min
        String excessiveJson = String.format("""
                {
                  "planDate": "2026-10-01",
                  "sessions": [
                    {
                      "taskId": %d,
                      "title": "Allocator Project",
                      "startTime": "2026-10-01T18:00:00",
                      "endTime": "2026-10-01T20:00:00",
                      "durationMinutes": 120,
                      "reason": "Over allocation"
                    }
                  ],
                  "totalStudyMinutes": 120,
                  "totalBreakMinutes": 0
                }
                """, taskA2.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(excessiveJson);

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(1.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertTrue(response.getFallbackUsed(), "Time violation must trigger fallback");
        assertTrue(response.getTotalStudyMinutes() <= 60);
    }

    @Test
    @DisplayName("14. Empty task list produces a valid empty plan without crashing")
    void emptyTaskList_producesValidEmptyPlan() {
        taskRepository.deleteAll();

        GenerateStudyPlanRequest request = GenerateStudyPlanRequest.builder()
                .availableHours(2.0)
                .startTime(referenceStartTime)
                .breakDurationMinutes(10)
                .build();

        StudyPlanResponse response = studyPlanService.generateStudyPlan(userA.getId(), request);

        assertNotNull(response);
        assertTrue(response.getSessions().isEmpty());
        assertEquals(0, response.getTotalStudyMinutes());
        assertTrue(response.getFallbackUsed());
        assertTrue(response.getMessage().contains("No incomplete tasks"));
    }
}
