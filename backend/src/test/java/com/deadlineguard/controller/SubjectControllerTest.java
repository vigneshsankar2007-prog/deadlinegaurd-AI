package com.deadlineguard.controller;

import com.deadlineguard.dto.subject.CreateSubjectRequest;
import com.deadlineguard.dto.subject.UpdateSubjectRequest;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.User;
import com.deadlineguard.repository.SubjectRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 5 Subject CRUD and user ownership security integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
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

        tokenA = jwtService.generateToken(userA.getEmail());
        tokenB = jwtService.generateToken(userB.getEmail());
    }

    @Test
    @DisplayName("1. Authenticated student can create a new academic subject")
    void authenticatedUser_canCreateSubject() throws Exception {
        CreateSubjectRequest request = CreateSubjectRequest.builder()
                .subjectName("Algorithms & Data Structures")
                .subjectCode("CS301")
                .credits(4)
                .colorHex("#2563EB")
                .build();

        mockMvc.perform(post("/api/v1/subjects")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subjectCode").value("CS301"))
                .andExpect(jsonPath("$.data.subjectName").value("Algorithms & Data Structures"))
                .andExpect(jsonPath("$.data.credits").value(4));
    }

    @Test
    @DisplayName("2. Authenticated student can list ONLY their own enrolled subjects")
    void authenticatedUser_canListOnlyOwnSubjects() throws Exception {
        subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Operating Systems")
                .subjectCode("CS302")
                .credits(4)
                .colorHex("#10B981")
                .build());

        subjectRepository.save(Subject.builder()
                .user(userB)
                .subjectName("Thermodynamics")
                .subjectCode("ME201")
                .credits(3)
                .colorHex("#F59E0B")
                .build());

        mockMvc.perform(get("/api/v1/subjects")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].subjectCode").value("CS302"));
    }

    @Test
    @DisplayName("3. Authenticated student can retrieve their own subject by ID")
    void authenticatedUser_canRetrieveOwnSubject() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Computer Networks")
                .subjectCode("CS401")
                .credits(3)
                .colorHex("#6366F1")
                .build());

        mockMvc.perform(get("/api/v1/subjects/" + subject.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(subject.getId()))
                .andExpect(jsonPath("$.data.subjectCode").value("CS401"));
    }

    @Test
    @DisplayName("4. Authenticated student can update their own subject")
    void authenticatedUser_canUpdateOwnSubject() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Database Systems")
                .subjectCode("CS501")
                .credits(3)
                .colorHex("#3B82F6")
                .build());

        UpdateSubjectRequest updateReq = UpdateSubjectRequest.builder()
                .subjectName("Advanced Database Systems")
                .subjectCode("CS501")
                .credits(4)
                .colorHex("#1D4ED8")
                .build();

        mockMvc.perform(put("/api/v1/subjects/" + subject.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subjectName").value("Advanced Database Systems"))
                .andExpect(jsonPath("$.data.credits").value(4));
    }

    @Test
    @DisplayName("5. Authenticated student can delete their own subject")
    void authenticatedUser_canDeleteOwnSubject() throws Exception {
        Subject subject = subjectRepository.save(Subject.builder()
                .user(userA)
                .subjectName("Compiler Design")
                .subjectCode("CS601")
                .credits(3)
                .colorHex("#8B5CF6")
                .build());

        mockMvc.perform(delete("/api/v1/subjects/" + subject.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/subjects/" + subject.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("6. Student cannot access, modify, or delete another student's subject")
    void studentCannotAccessAnotherStudentsSubject() throws Exception {
        Subject bSubject = subjectRepository.save(Subject.builder()
                .user(userB)
                .subjectName("Fluid Mechanics")
                .subjectCode("ME301")
                .credits(4)
                .colorHex("#EF4444")
                .build());

        // Alice attempts to GET Bob's subject -> 404 Not Found (does not leak existence)
        mockMvc.perform(get("/api/v1/subjects/" + bSubject.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());

        // Alice attempts to PUT Bob's subject -> 404 Not Found
        UpdateSubjectRequest updateReq = UpdateSubjectRequest.builder()
                .subjectName("Hacked Subject")
                .subjectCode("ME301")
                .credits(4)
                .colorHex("#EF4444")
                .build();

        mockMvc.perform(put("/api/v1/subjects/" + bSubject.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // Alice attempts to DELETE Bob's subject -> 404 Not Found
        mockMvc.perform(delete("/api/v1/subjects/" + bSubject.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("7. Unauthenticated request to subject endpoints is rejected with 401")
    void unauthenticatedSubjectRequest_rejected() throws Exception {
        mockMvc.perform(get("/api/v1/subjects"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
