package com.deadlineguard.service;

import com.deadlineguard.dto.ai.AIRecommendationResponse;
import com.deadlineguard.entity.AIRecommendation;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.AIRecommendationRepository;
import com.deadlineguard.repository.SubjectRepository;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.service.ai.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Stage 7: AIService unit & integration tests with deterministic mock Gemini provider.
 * Guarantees zero reliance on live internet access during automated build runs.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AIServiceTest {

    @Autowired
    private AIService aiService;

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

    @BeforeEach
    void setUp() {
        aiRecommendationRepository.deleteAll();
        taskPriorityRepository.deleteAll();
        taskRepository.deleteAll();
        subjectRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(User.builder()
                .name("Student Alice")
                .email("alice@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Computer Science")
                .semester(5)
                .college("Engineering College")
                .build());

        userB = userRepository.save(User.builder()
                .name("Student Bob")
                .email("bob@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Mechanical Engineering")
                .semester(3)
                .college("Engineering College")
                .build());

        subjectA = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Operating Systems")
                .subjectCode("CS302")
                .credits(4)
                .colorHex("#10B981")
                .build());

        // Low priority task (easy, low weight, long deadline)
        taskA1 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Reading Assignment Chapter 1")
                .taskType(TaskType.READING)
                .deadline(LocalDateTime.now().plusDays(10))
                .difficulty(1)
                .academicWeight(new BigDecimal("2.00"))
                .estimatedHours(new BigDecimal("1.0"))
                .status(TaskStatus.PENDING)
                .build());

        // High priority task (difficult, high weight, imminent deadline)
        taskA2 = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Kernel Page Replacement Project")
                .taskType(TaskType.PROJECT)
                .deadline(LocalDateTime.now().plusHours(36))
                .difficulty(5)
                .academicWeight(new BigDecimal("30.00"))
                .estimatedHours(new BigDecimal("10.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build());

        // Bob's task (foreign user)
        taskB1 = taskRepository.save(Task.builder()
                .user(userB)
                .title("Thermodynamics Lab Report")
                .taskType(TaskType.LAB)
                .deadline(LocalDateTime.now().plusDays(2))
                .difficulty(4)
                .academicWeight(new BigDecimal("20.00"))
                .estimatedHours(new BigDecimal("6.0"))
                .status(TaskStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("4. Gemini structured response is parsed correctly")
    void geminiStructuredResponseIsParsed() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);

        String mockGeminiJson = String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Focus on Kernel Page Replacement Project",
                  "aiReasoning": "Due in 36 hours and has 30%% academic weight with highest urgency score.",
                  "suggestedAction": "Implement the clock replacement algorithm in the next 2-hour study block.",
                  "taskId": %d
                }
                """, taskA2.getId());

        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(mockGeminiJson);

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertFalse(response.getFallbackUsed());
        assertEquals("Focus on Kernel Page Replacement Project", response.getRecommendationText());
        assertEquals(taskA2.getId(), response.getTaskId());
        assertEquals(taskA2.getTitle(), response.getTaskTitle());
        assertEquals("WHAT_SHOULD_I_DO_NOW", response.getRecommendationType());
    }

    @Test
    @DisplayName("3. Recommendation contains real application task context in prompt")
    void recommendationContainsRealTaskContext() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Work on Operating Systems Project",
                  "aiReasoning": "Highest priority deliverable.",
                  "suggestedAction": "Code page replacement.",
                  "taskId": %d
                }
                """, taskA2.getId()));

        aiService.generateWhatShouldIDoNow(userA.getId());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiClient).generateContent(anyString(), promptCaptor.capture());

        String promptSent = promptCaptor.getValue();
        // Verify real application data was embedded
        assertTrue(promptSent.contains("Kernel Page Replacement Project"));
        assertTrue(promptSent.contains("CS302"));
        assertTrue(promptSent.contains("priorityScore"));
        // Verify credentials/hashes are NEVER present
        assertFalse(promptSent.contains("passwordHash"));
        assertFalse(promptSent.contains("alice@university.edu"));
    }

    @Test
    @DisplayName("5. Malformed Gemini response triggers deterministic fallback")
    void malformedGeminiResponseTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn("INVALID_NOT_A_JSON_STRING");

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed(), "Fallback should be activated for malformed JSON");
        assertTrue(response.getRecommendationText().contains(taskA2.getTitle()));
    }

    @Test
    @DisplayName("6. Missing API key triggers deterministic fallback")
    void missingApiKeyTriggersFallback() {
        when(geminiClient.isConfigured()).thenReturn(false);

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        assertEquals(taskA2.getId(), response.getTaskId());
        verifyNoInteractions(geminiClient);
    }

    @Test
    @DisplayName("7. Gemini timeout triggers deterministic fallback")
    void geminiTimeoutTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenThrow(new TimeoutException("Gemini API call timed out after 10000ms"));

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        assertEquals(taskA2.getId(), response.getTaskId());
    }

    @Test
    @DisplayName("8. Gemini server error triggers deterministic fallback")
    void geminiServerErrorTriggersFallback() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString()))
                .thenThrow(new RuntimeException("503 Service Unavailable"));

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        assertEquals(taskA2.getId(), response.getTaskId());
    }

    @Test
    @DisplayName("9. Recommendation is persisted to ai_recommendations database table")
    void recommendationIsPersistedToAIRecommendations() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Finish OS Project",
                  "aiReasoning": "Immediate deadline approaching.",
                  "suggestedAction": "Test edge cases.",
                  "taskId": %d
                }
                """, taskA2.getId()));

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        List<AIRecommendation> stored = aiRecommendationRepository.findByUserIdOrderByGeneratedAtDesc(userA.getId());
        assertEquals(1, stored.size());
        assertEquals(response.getId(), stored.get(0).getId());
        assertEquals("Finish OS Project", stored.get(0).getRecommendationText());
    }

    @Test
    @DisplayName("14. Gemini cannot inject nonexistent taskId (triggers fallback)")
    void geminiCannotInjectNonexistentTaskId() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        // Gemini hallucinates a taskId 99999 that does not exist
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Hallucinated Task Recommendation",
                  "aiReasoning": "Model hallucinated task id 99999.",
                  "suggestedAction": "Check nonexistent task.",
                  "taskId": 99999
                }
                """);

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed(), "Nonexistent taskId must be rejected and trigger fallback");
        assertEquals(taskA2.getId(), response.getTaskId());
    }

    @Test
    @DisplayName("15. Gemini cannot reference another user's taskId (triggers fallback)")
    void geminiCannotReferenceAnotherUsersTask() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        // Gemini attempts to return Bob's task ID (taskB1) for Alice
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Cross Tenant Task Recommendation",
                  "aiReasoning": "Referencing another student's task.",
                  "suggestedAction": "Unauthorized access attempt.",
                  "taskId": %d
                }
                """, taskB1.getId()));

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed(), "Cross-tenant task ID must be rejected and trigger fallback");
        assertEquals(taskA2.getId(), response.getTaskId());
    }

    @Test
    @DisplayName("16. Fallback selects highest-priority incomplete task (Stage 6 integration)")
    void fallbackSelectsHighestPriorityIncompleteTask() {
        when(geminiClient.isConfigured()).thenReturn(false);

        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userA.getId());

        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        // taskA2 has higher priority than taskA1
        assertEquals(taskA2.getId(), response.getTaskId());
        assertTrue(response.getRecommendationText().contains(taskA2.getTitle()));
    }
}
