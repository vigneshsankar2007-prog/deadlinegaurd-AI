package com.deadlineguard.controller;

import com.deadlineguard.entity.AIRecommendation;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.AIRecommendationRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.JwtService;
import com.deadlineguard.service.ai.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 7 AI Controller integration & security tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AIRecommendationRepository aiRecommendationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private GeminiClient geminiClient;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private Task taskA;
    private Task taskB;

    @BeforeEach
    void setUp() {
        aiRecommendationRepository.deleteAll();
        taskRepository.deleteAll();
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

        tokenA = jwtService.generateToken(userA.getEmail());
        tokenB = jwtService.generateToken(userB.getEmail());

        taskA = taskRepository.save(Task.builder()
                .user(userA)
                .title("Algorithms Homework 3")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(2))
                .difficulty(4)
                .academicWeight(new BigDecimal("15.00"))
                .estimatedHours(new BigDecimal("5.0"))
                .status(TaskStatus.PENDING)
                .build());

        taskB = taskRepository.save(Task.builder()
                .user(userB)
                .title("Robotics Final Design")
                .taskType(TaskType.PROJECT)
                .deadline(LocalDateTime.now().plusDays(5))
                .difficulty(5)
                .academicWeight(new BigDecimal("35.00"))
                .estimatedHours(new BigDecimal("15.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build());
    }

    @Test
    @DisplayName("1. Authenticated student can request 'What should I do now?'")
    void authenticatedUser_canRequestWhatShouldIDoNow() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Work on Algorithms Homework 3",
                  "aiReasoning": "Due in 2 days and carries 15%% of your grade.",
                  "suggestedAction": "Solve dynamic programming problems 1 and 2.",
                  "taskId": %d
                }
                """, taskA.getId()));

        mockMvc.perform(post("/api/v1/ai/what-should-i-do-now")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendationType").value("WHAT_SHOULD_I_DO_NOW"))
                .andExpect(jsonPath("$.data.recommendationText").value("Work on Algorithms Homework 3"))
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()))
                .andExpect(jsonPath("$.data.fallbackUsed").value(false));
    }

    @Test
    @DisplayName("2. Recommendation uses authenticated user's tasks only")
    void recommendationUsesAuthenticatedUsersTasksOnly() throws Exception {
        when(geminiClient.isConfigured()).thenReturn(true);
        when(geminiClient.generateContent(anyString(), anyString())).thenReturn(String.format("""
                {
                  "recommendationType": "WHAT_SHOULD_I_DO_NOW",
                  "recommendationText": "Focus on Alice Task",
                  "aiReasoning": "Top task.",
                  "suggestedAction": "Proceed with study block.",
                  "taskId": %d
                }
                """, taskA.getId()));

        mockMvc.perform(post("/api/v1/ai/what-should-i-do-now")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()))
                .andExpect(jsonPath("$.data.userId").value(userA.getId()));
    }

    @Test
    @DisplayName("10. Authenticated student can retrieve own historical recommendations")
    void authenticatedUser_canRetrieveOwnRecommendations() throws Exception {
        aiRecommendationRepository.save(AIRecommendation.builder()
                .user(userA)
                .task(taskA)
                .recommendationType("WHAT_SHOULD_I_DO_NOW")
                .recommendationText("Alice Historical Rec")
                .aiReasoning("Reasoning text")
                .suggestedAction("Action text")
                .generatedAt(LocalDateTime.now())
                .build());

        aiRecommendationRepository.save(AIRecommendation.builder()
                .user(userB)
                .task(taskB)
                .recommendationType("WHAT_SHOULD_I_DO_NOW")
                .recommendationText("Bob Historical Rec")
                .aiReasoning("Reasoning text")
                .suggestedAction("Action text")
                .generatedAt(LocalDateTime.now())
                .build());

        // Alice retrieves her recommendations list
        mockMvc.perform(get("/api/v1/ai/recommendations")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].recommendationText").value("Alice Historical Rec"));
    }

    @Test
    @DisplayName("11. User cannot retrieve another user's recommendation (returns 404 without leaking)")
    void userCannotRetrieveAnotherUsersRecommendation() throws Exception {
        AIRecommendation bobRec = aiRecommendationRepository.save(AIRecommendation.builder()
                .user(userB)
                .task(taskB)
                .recommendationType("WHAT_SHOULD_I_DO_NOW")
                .recommendationText("Bob Confidential Recommendation")
                .aiReasoning("Private reasoning")
                .suggestedAction("Private action")
                .generatedAt(LocalDateTime.now())
                .build());

        // Alice attempts to GET Bob's recommendation by ID -> 404 Not Found
        mockMvc.perform(get("/api/v1/ai/recommendations/" + bobRec.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("13. Unauthenticated AI requests return 401 Unauthorized")
    void unauthenticatedAIRequestReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/ai/what-should-i-do-now"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/ai/recommendations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/ai/recommendations/1"))
                .andExpect(status().isUnauthorized());
    }
}
