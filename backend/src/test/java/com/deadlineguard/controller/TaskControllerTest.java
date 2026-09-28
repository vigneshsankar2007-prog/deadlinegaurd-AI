package com.deadlineguard.controller;

import com.deadlineguard.dto.task.CreateTaskRequest;
import com.deadlineguard.dto.task.UpdateTaskRequest;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.SubjectRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 5 Task CRUD and user ownership security integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private Subject subjectA;
    private Subject subjectB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
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
                .subjectName("Algorithms")
                .subjectCode("CS301")
                .credits(4)
                .colorHex("#2563EB")
                .build());

        subjectB = subjectRepository.save(Subject.builder()
                .user(userB)
                .subjectName("Thermodynamics")
                .subjectCode("ME201")
                .credits(3)
                .colorHex("#F59E0B")
                .build());

        tokenA = jwtService.generateToken(userA.getEmail());
        tokenB = jwtService.generateToken(userB.getEmail());
    }

    @Test
    @DisplayName("8. Authenticated student can create a new academic deliverable/task")
    void authenticatedUser_canCreateTask() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Dynamic Programming Assignment")
                .description("Implement 0/1 knapsack and longest common subsequence")
                .subjectId(subjectA.getId())
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(5))
                .difficulty(4)
                .academicWeight(new BigDecimal("15.00"))
                .estimatedHours(new BigDecimal("6.0"))
                .status(TaskStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Dynamic Programming Assignment"))
                .andExpect(jsonPath("$.data.subjectId").value(subjectA.getId()))
                .andExpect(jsonPath("$.data.difficulty").value(4))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("9. Authenticated student can list ONLY their own tasks")
    void authenticatedUser_canListOnlyOwnTasks() throws Exception {
        taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Alice Task 1")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(3))
                .difficulty(3)
                .academicWeight(new BigDecimal("10.00"))
                .estimatedHours(new BigDecimal("2.0"))
                .status(TaskStatus.PENDING)
                .build());

        taskRepository.save(Task.builder()
                .user(userB)
                .subject(subjectB)
                .title("Bob Task 1")
                .taskType(TaskType.PROJECT)
                .deadline(LocalDateTime.now().plusDays(7))
                .difficulty(5)
                .academicWeight(new BigDecimal("25.00"))
                .estimatedHours(new BigDecimal("10.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build());

        mockMvc.perform(get("/api/v1/tasks")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Alice Task 1"));
    }

    @Test
    @DisplayName("10. Authenticated student can retrieve their own task by ID")
    void authenticatedUser_canRetrieveOwnTask() throws Exception {
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Sorting Algorithms Lab")
                .taskType(TaskType.LAB)
                .deadline(LocalDateTime.now().plusDays(2))
                .difficulty(2)
                .academicWeight(new BigDecimal("5.00"))
                .estimatedHours(new BigDecimal("1.5"))
                .status(TaskStatus.PENDING)
                .build());

        mockMvc.perform(get("/api/v1/tasks/" + task.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(task.getId()))
                .andExpect(jsonPath("$.data.title").value("Sorting Algorithms Lab"));
    }

    @Test
    @DisplayName("11. Authenticated student can update their own task")
    void authenticatedUser_canUpdateOwnTask() throws Exception {
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .subject(subjectA)
                .title("Midterm Exam Prep")
                .taskType(TaskType.EXAM)
                .deadline(LocalDateTime.now().plusDays(4))
                .difficulty(4)
                .academicWeight(new BigDecimal("30.00"))
                .estimatedHours(new BigDecimal("8.0"))
                .status(TaskStatus.PENDING)
                .build());

        UpdateTaskRequest updateReq = UpdateTaskRequest.builder()
                .title("Midterm Exam Prep (Updated)")
                .description("Cover chapters 1 through 5 thoroughly")
                .subjectId(subjectA.getId())
                .taskType(TaskType.EXAM)
                .deadline(LocalDateTime.now().plusDays(6))
                .difficulty(5)
                .academicWeight(new BigDecimal("35.00"))
                .estimatedHours(new BigDecimal("12.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + task.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Midterm Exam Prep (Updated)"))
                .andExpect(jsonPath("$.data.difficulty").value(5))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("12. Authenticated student can delete their own task")
    void authenticatedUser_canDeleteOwnTask() throws Exception {
        Task task = taskRepository.save(Task.builder()
                .user(userA)
                .title("Optional Seminar Note")
                .taskType(TaskType.OTHER)
                .deadline(LocalDateTime.now().plusDays(10))
                .difficulty(1)
                .academicWeight(new BigDecimal("0.00"))
                .estimatedHours(new BigDecimal("0.5"))
                .status(TaskStatus.PENDING)
                .build());

        mockMvc.perform(delete("/api/v1/tasks/" + task.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/tasks/" + task.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("13. Student cannot access, update, or delete another student's task")
    void studentCannotAccessAnotherStudentsTask() throws Exception {
        Task bTask = taskRepository.save(Task.builder()
                .user(userB)
                .subject(subjectB)
                .title("Heat Transfer Project")
                .taskType(TaskType.PROJECT)
                .deadline(LocalDateTime.now().plusDays(14))
                .difficulty(5)
                .academicWeight(new BigDecimal("40.00"))
                .estimatedHours(new BigDecimal("20.0"))
                .status(TaskStatus.IN_PROGRESS)
                .build());

        // Alice attempts to GET Bob's task -> 404 Not Found
        mockMvc.perform(get("/api/v1/tasks/" + bTask.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Alice attempts to PUT Bob's task -> 404 Not Found
        UpdateTaskRequest updateReq = UpdateTaskRequest.builder()
                .title("Malicious Edit")
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(1))
                .difficulty(1)
                .academicWeight(new BigDecimal("0.00"))
                .estimatedHours(new BigDecimal("0.5"))
                .status(TaskStatus.COMPLETED)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + bTask.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Alice attempts to DELETE Bob's task -> 404 Not Found
        mockMvc.perform(delete("/api/v1/tasks/" + bTask.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("14. Student CANNOT create a task linked to another student's subject")
    void studentCannotCreateTaskWithAnotherStudentsSubject() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Unauthorized Cross-Tenant Assignment")
                .subjectId(subjectB.getId()) // Bob's subject!
                .taskType(TaskType.ASSIGNMENT)
                .deadline(LocalDateTime.now().plusDays(3))
                .difficulty(3)
                .academicWeight(new BigDecimal("10.00"))
                .estimatedHours(new BigDecimal("2.0"))
                .status(TaskStatus.PENDING)
                .build();

        // Alice tries to use Bob's subject -> 404 Not Found
        mockMvc.perform(post("/api/v1/tasks")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("15. Unauthenticated request to task endpoints is rejected with 401")
    void unauthenticatedTaskRequest_rejected() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("16. Invalid task input returns 400 Bad Request with field validation errors")
    void invalidTaskInput_returnsBadRequest() throws Exception {
        CreateTaskRequest invalidReq = CreateTaskRequest.builder()
                .title("") // Blank title violates @NotBlank
                .deadline(null) // Missing deadline violates @NotNull
                .difficulty(10) // Difficulty > 5 violates @Max(5)
                .academicWeight(new BigDecimal("150.00")) // Weight > 100 violates @DecimalMax
                .estimatedHours(new BigDecimal("0.0")) // Hours <= 0 violates @DecimalMin("0.1")
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.deadline").exists())
                .andExpect(jsonPath("$.validationErrors.difficulty").exists())
                .andExpect(jsonPath("$.validationErrors.academicWeight").exists())
                .andExpect(jsonPath("$.validationErrors.estimatedHours").exists());
    }
}
