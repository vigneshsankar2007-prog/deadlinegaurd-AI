package com.deadlineguard.controller;

import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Stage 6 Priority Controller & Ownership Security Tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PriorityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private Task taskA;
    private Task taskB;

    @BeforeEach
    void setUp() {
        taskPriorityRepository.deleteAll();
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
    @DisplayName("12. Authenticated student can retrieve their own task priority")
    void authenticatedUser_canRetrieveOwnTaskPriority() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + taskA.getId() + "/priority")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()))
                .andExpect(jsonPath("$.data.priorityScore").exists())
                .andExpect(jsonPath("$.data.priorityTier").exists())
                .andExpect(jsonPath("$.data.urgencyScore").exists())
                .andExpect(jsonPath("$.data.difficultyScore").exists())
                .andExpect(jsonPath("$.data.academicWeightScore").exists())
                .andExpect(jsonPath("$.data.effortScore").exists())
                .andExpect(jsonPath("$.data.workloadScore").exists())
                .andExpect(jsonPath("$.data.explanationText").exists());
    }

    @Test
    @DisplayName("13. Authenticated student can recalculate their own task priority")
    void authenticatedUser_canRecalculateOwnTaskPriority() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/" + taskA.getId() + "/priority/recalculate")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()))
                .andExpect(jsonPath("$.data.priorityScore").isNumber());
    }

    @Test
    @DisplayName("14. Student cannot retrieve another student's task priority (returns 404 without leaking)")
    void studentCannotRetrieveAnotherStudentsTaskPriority() throws Exception {
        // Alice attempts to access Bob's task priority -> 404 Not Found
        mockMvc.perform(get("/api/v1/tasks/" + taskB.getId() + "/priority")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("15. Student cannot recalculate another student's task priority (returns 404 without leaking)")
    void studentCannotRecalculateAnotherStudentsTaskPriority() throws Exception {
        // Alice attempts to recalculate Bob's task priority -> 404 Not Found
        mockMvc.perform(post("/api/v1/tasks/" + taskB.getId() + "/priority/recalculate")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("16. Unauthenticated priority requests return 401 Unauthorized")
    void unauthenticatedPriorityRequest_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/" + taskA.getId() + "/priority"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/tasks/" + taskA.getId() + "/priority/recalculate"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/priorities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("17. Authenticated student can list only their own task priorities")
    void authenticatedUser_canListOnlyOwnTaskPriorities() throws Exception {
        // Force priority calculation for both tasks
        mockMvc.perform(post("/api/v1/tasks/" + taskA.getId() + "/priority/recalculate")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/tasks/" + taskB.getId() + "/priority/recalculate")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        // Alice retrieves priority list -> sees only taskA (size 1)
        mockMvc.perform(get("/api/v1/priorities")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].taskId").value(taskA.getId()));
    }
}
