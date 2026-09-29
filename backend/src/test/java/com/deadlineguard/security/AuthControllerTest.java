package com.deadlineguard.security;

import com.deadlineguard.dto.auth.LoginRequest;
import com.deadlineguard.dto.auth.RegisterRequest;
import com.deadlineguard.entity.User;
import com.deadlineguard.repository.UserRepository;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Stage 4 Security & Authentication integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Register with valid data returns 201, JWT token, and safe UserResponse")
    void register_validData_returnsCreated() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Johnson")
                .email("alex.johnson@mit.edu")
                .password("SecurePass123!")
                .department("Computer Science")
                .semester(6)
                .college("MIT")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("alex.johnson@mit.edu"))
                .andExpect(jsonPath("$.data.user.name").value("Alex Johnson"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.user.password").doesNotExist());

        // Verify password is BCrypt hashed in database
        User savedUser = userRepository.findByEmail("alex.johnson@mit.edu").orElseThrow();
        assertTrue(passwordEncoder.matches("SecurePass123!", savedUser.getPasswordHash()));
        assertTrue(savedUser.getPasswordHash().startsWith("$2a$") || savedUser.getPasswordHash().startsWith("$2b$"));
    }

    @Test
    @DisplayName("2. Register duplicate email returns 409 Conflict")
    void register_duplicateEmail_returnsConflict() throws Exception {
        User existingUser = User.builder()
                .name("Existing Student")
                .email("alex.johnson@mit.edu")
                .passwordHash(passwordEncoder.encode("ExistingPass123!"))
                .department("Computer Science")
                .semester(4)
                .college("MIT")
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Johnson")
                .email("alex.johnson@mit.edu")
                .password("NewPass123!")
                .department("Computer Science")
                .semester(6)
                .college("MIT")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("3. Register invalid email format returns 400 Bad Request")
    void register_invalidEmail_returnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Johnson")
                .email("not-a-valid-email")
                .password("SecurePass123!")
                .department("Computer Science")
                .semester(6)
                .college("MIT")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()));
    }

    @Test
    @DisplayName("4. Register invalid semester (>12) returns 400 Bad Request")
    void register_invalidSemester_returnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alex Johnson")
                .email("alex.johnson@mit.edu")
                .password("SecurePass123!")
                .department("Computer Science")
                .semester(15) // Violates chk_users_semester (1-12)
                .college("MIT")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.semester", notNullValue()));
    }

    @Test
    @DisplayName("5. Login with valid credentials returns 200 OK and JWT access token")
    void login_validCredentials_returnsOk() throws Exception {
        User user = User.builder()
                .name("Priya Sharma")
                .email("priya.sharma@stanford.edu")
                .passwordHash(passwordEncoder.encode("Stanford2026!"))
                .department("Electrical Engineering")
                .semester(4)
                .college("Stanford University")
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("priya.sharma@stanford.edu")
                .password("Stanford2026!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email").value("priya.sharma@stanford.edu"))
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("6. Login with invalid password returns generic error without credential details")
    void login_invalidPassword_returnsGenericFailure() throws Exception {
        User user = User.builder()
                .name("Priya Sharma")
                .email("priya.sharma@stanford.edu")
                .passwordHash(passwordEncoder.encode("CorrectPassword123!"))
                .department("Electrical Engineering")
                .semester(4)
                .college("Stanford University")
                .build();
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("priya.sharma@stanford.edu")
                .password("WrongPassword999!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("7. Protected /api/v1/auth/me rejects unauthenticated request with 401 Unauthorized")
    void protectedEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("8. Protected /api/v1/auth/me authenticates with valid Bearer JWT")
    void protectedEndpoint_validJwt_returnsUser() throws Exception {
        User user = User.builder()
                .name("Marcus Vance")
                .email("marcus.vance@cmu.edu")
                .passwordHash(passwordEncoder.encode("CMUSecure2026!"))
                .department("Robotics")
                .semester(2)
                .college("Carnegie Mellon University")
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("marcus.vance@cmu.edu"))
                .andExpect(jsonPath("$.data.name").value("Marcus Vance"))
                .andExpect(jsonPath("$.data.department").value("Robotics"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("9. Protected /api/v1/auth/me rejects malformed JWT with 401 Unauthorized")
    void protectedEndpoint_malformedJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid.malformed.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("10. Public health endpoint /api/v1/health remains accessible without authentication")
    void healthEndpoint_remainsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
