package com.deadlineguard.controller;

import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.*;
import com.deadlineguard.security.JwtService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 10: AnalyticsController integration and security tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private Clock clock;

    private User user;
    private String token;
    private final Instant fixedInstant = Instant.parse("2026-10-07T12:00:00Z");

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        productivityStatRepository.deleteAll();
        studySessionRepository.deleteAll();
        taskRepository.deleteAll();
        subjectRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .name("Student Alice")
                .email("alice@university.edu")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .department("Computer Science")
                .semester(5)
                .college("Engineering College")
                .build());

        token = jwtService.generateToken(user.getEmail());
    }

    @Test
    @DisplayName("1. Authenticated user can get dashboard analytics")
    void authenticatedUser_canGetDashboardAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTasks").value(0))
                .andExpect(jsonPath("$.data.completionPercentage").value(0.0))
                .andExpect(jsonPath("$.data.totalStudyMinutes").value(0));
    }

    @Test
    @DisplayName("2. Authenticated user can get weekly analytics with 7 days")
    void authenticatedUser_canGetWeeklyAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/weekly")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyPoints", hasSize(7)))
                .andExpect(jsonPath("$.data.endDate").value("2026-10-07"))
                .andExpect(jsonPath("$.data.startDate").value("2026-10-01"));
    }

    @Test
    @DisplayName("3. Authenticated user can get subject analytics")
    void authenticatedUser_canGetSubjectAnalytics() throws Exception {
        Subject sub = subjectRepository.save(Subject.builder()
                .user(user)
                .subjectName("Computer Systems")
                .subjectCode("CS301")
                .credits(4)
                .colorHex("#10B981")
                .build());

        taskRepository.save(Task.builder()
                .user(user)
                .subject(sub)
                .title("Kernels & Interrupts")
                .taskType(TaskType.LAB)
                .estimatedHours(new BigDecimal("4.0"))
                .status(TaskStatus.COMPLETED)
                .deadline(LocalDateTime.ofInstant(fixedInstant, ZoneOffset.UTC).plusDays(1))
                .build());

        mockMvc.perform(get("/api/v1/analytics/subjects")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].subjectCode").value("CS301"))
                .andExpect(jsonPath("$.data[0].taskCount").value(1))
                .andExpect(jsonPath("$.data[0].completedTaskCount").value(1))
                .andExpect(jsonPath("$.data[0].completionPercentage").value(100.0));
    }

    @Test
    @DisplayName("4. Unauthenticated analytics request returns 401 Unauthorized")
    void unauthenticatedAnalyticsRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/weekly"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/subjects"))
                .andExpect(status().isUnauthorized());
    }
}
