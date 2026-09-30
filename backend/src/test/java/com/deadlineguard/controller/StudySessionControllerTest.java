package com.deadlineguard.controller;

import com.deadlineguard.dto.study.StartSessionRequest;
import com.deadlineguard.dto.study.StopSessionRequest;
import com.deadlineguard.entity.StudySession;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.StudySessionRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 8: StudySessionController integration & security tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudySessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private Task taskA;
    private Task taskB;

    @BeforeEach
    void setUp() {
        studySessionRepository.deleteAll();
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
                .title("Alice Linear Algebra Assignment")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(2))
                .difficulty(3)
                .academicWeight(new BigDecimal("15.00"))
                .estimatedHours(new BigDecimal("4.0"))
                .status(TaskStatus.PENDING)
                .build());

        taskB = taskRepository.save(Task.builder()
                .user(userB)
                .title("Bob Thermodynamics Lab Report")
                .taskType(TaskType.LAB)
                .deadline(LocalDateTime.now().plusDays(4))
                .difficulty(4)
                .academicWeight(new BigDecimal("20.00"))
                .estimatedHours(new BigDecimal("6.0"))
                .status(TaskStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Authenticated student can start, retrieve active, and stop focus session")
    void studentCanStart_retrieveActive_andStopSession() throws Exception {
        StartSessionRequest startReq = StartSessionRequest.builder()
                .taskId(taskA.getId())
                .notes("Solving linear system equations")
                .build();

        // 1. Start session
        String startResponse = mockMvc.perform(post("/api/v1/study-sessions/start")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()))
                .andExpect(jsonPath("$.data.isCompleted").value(false))
                .andReturn().getResponse().getContentAsString();

        Long sessionId = objectMapper.readTree(startResponse).path("data").path("id").asLong();

        // 2. Query active session
        mockMvc.perform(get("/api/v1/study-sessions/active")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionId))
                .andExpect(jsonPath("$.data.isCompleted").value(false));

        // 3. Stop session
        StopSessionRequest stopReq = StopSessionRequest.builder().notes("Completed successfully").build();
        mockMvc.perform(post("/api/v1/study-sessions/" + sessionId + "/stop")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stopReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(sessionId))
                .andExpect(jsonPath("$.data.isCompleted").value(true))
                .andExpect(jsonPath("$.data.notes").value("Completed successfully"));
    }

    @Test
    @DisplayName("24. Unauthenticated session request returns 401 Unauthorized")
    void unauthenticatedSessionRequest_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/study-sessions/start"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/study-sessions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/study-sessions/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/study-sessions/1/stop"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Starting multiple active sessions returns 409 Conflict")
    void startingMultipleActiveSessions_returns409Conflict() throws Exception {
        // Start first session
        mockMvc.perform(post("/api/v1/study-sessions/start")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());

        // Attempt second active session -> 409 Conflict
        mockMvc.perform(post("/api/v1/study-sessions/start")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("User cannot stop or view another user's session (returns 404)")
    void userCannotStopOrViewAnotherUsersSession() throws Exception {
        StudySession bobSession = studySessionRepository.save(StudySession.builder()
                .user(userB)
                .task(taskB)
                .startTime(LocalDateTime.now())
                .durationMinutes(0)
                .isCompleted(false)
                .build());

        // Alice attempts to GET Bob's session -> 404
        mockMvc.perform(get("/api/v1/study-sessions/" + bobSession.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Alice attempts to STOP Bob's session -> 404
        mockMvc.perform(post("/api/v1/study-sessions/" + bobSession.getId() + "/stop")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Authenticated student lists only their own sessions")
    void studentListsOnlyOwnSessions() throws Exception {
        studySessionRepository.save(StudySession.builder()
                .user(userA)
                .startTime(LocalDateTime.now())
                .durationMinutes(30)
                .isCompleted(true)
                .build());

        studySessionRepository.save(StudySession.builder()
                .user(userB)
                .startTime(LocalDateTime.now())
                .durationMinutes(60)
                .isCompleted(true)
                .build());

        mockMvc.perform(get("/api/v1/study-sessions")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
}
