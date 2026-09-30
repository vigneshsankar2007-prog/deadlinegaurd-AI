package com.deadlineguard.controller;

import com.deadlineguard.entity.Notification;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.NotificationType;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.repository.NotificationRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 9: NotificationController REST integration and security tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;
    private Task taskA;
    private Notification notifA;
    private Notification notifB;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
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
                .title("Operating Systems Project")
                .taskType(TaskType.PROJECT)
                .deadline(LocalDateTime.now().plusDays(1))
                .status(TaskStatus.PENDING)
                .difficulty(3)
                .build());

        notifA = notificationRepository.save(Notification.builder()
                .user(userA)
                .task(taskA)
                .title("Deadline in 24 Hours")
                .message("Review remaining work")
                .notificationType(NotificationType.DEADLINE_24H)
                .isRead(false)
                .build());

        notifB = notificationRepository.save(Notification.builder()
                .user(userB)
                .title("Bob's Alert")
                .message("Private message for Bob")
                .notificationType(NotificationType.DAILY_SUMMARY)
                .isRead(false)
                .build());
    }

    @Test
    @DisplayName("1. Authenticated user can list own notifications")
    void authenticatedUser_canListOwnNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(notifA.getId()))
                .andExpect(jsonPath("$.data[0].title").value("Deadline in 24 Hours"));
    }

    @Test
    @DisplayName("2. Authenticated user can list unread notifications")
    void authenticatedUser_canListUnreadNotifications() throws Exception {
        // Mark notifA as read
        notifA.setIsRead(true);
        notificationRepository.save(notifA);

        // Add second unread notification for userA
        Notification unreadNotif = notificationRepository.save(Notification.builder()
                .user(userA)
                .task(taskA)
                .title("Deadline in 6 Hours")
                .message("Urgent countdown")
                .notificationType(NotificationType.DEADLINE_6H)
                .isRead(false)
                .build());

        mockMvc.perform(get("/api/v1/notifications/unread")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(unreadNotif.getId()));
    }

    @Test
    @DisplayName("3. Authenticated user can read own notification by ID")
    void authenticatedUser_canReadOwnNotification() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/" + notifA.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notifA.getId()))
                .andExpect(jsonPath("$.data.taskId").value(taskA.getId()));
    }

    @Test
    @DisplayName("4. Authenticated user can mark own notification as read")
    void authenticatedUser_canMarkOwnNotificationAsRead() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/" + notifA.getId() + "/read")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notifA.getId()))
                .andExpect(jsonPath("$.data.isRead").value(true));

        Notification reloaded = notificationRepository.findById(notifA.getId()).orElseThrow();
        assertTrue(reloaded.getIsRead());
    }

    @Test
    @DisplayName("5. Authenticated user can mark all notifications as read")
    void authenticatedUser_canMarkAllNotificationsAsRead() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/read-all")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.markedCount").value(1));

        Notification reloaded = notificationRepository.findById(notifA.getId()).orElseThrow();
        assertTrue(reloaded.getIsRead());

        // Bob's notification should remain unread
        Notification bobsReloaded = notificationRepository.findById(notifB.getId()).orElseThrow();
        assertFalse(bobsReloaded.getIsRead());
    }

    @Test
    @DisplayName("6. User cannot access another user's notification (returns 404 without leaking)")
    void userCannotAccessAnotherUsersNotification() throws Exception {
        // Alice attempts to GET Bob's notification
        mockMvc.perform(get("/api/v1/notifications/" + notifB.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        // Alice attempts to mark Bob's notification as read
        mockMvc.perform(patch("/api/v1/notifications/" + notifB.getId() + "/read")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("7. Unauthenticated notification request returns 401 Unauthorized")
    void unauthenticatedNotificationRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/notifications/unread"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/notifications/1/read"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isUnauthorized());
    }
}
