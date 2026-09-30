package com.deadlineguard.service;

import com.deadlineguard.dto.study.StartSessionRequest;
import com.deadlineguard.dto.study.StopSessionRequest;
import com.deadlineguard.dto.study.StudySessionResponse;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.entity.enums.TaskType;
import com.deadlineguard.exception.BadRequestException;
import com.deadlineguard.exception.DuplicateResourceException;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.StudySessionRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 8: StudySessionService unit & integration tests.
 * All tests use controlled fixed timestamps to guarantee deterministic timing.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudySessionServiceTest {

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Task taskA;
    private Task taskB;
    private final LocalDateTime baseTime = LocalDateTime.of(2026, 10, 1, 10, 0, 0);

    @BeforeEach
    void setUp() {
        studySessionRepository.deleteAll();
        taskRepository.deleteAll();
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

        taskA = taskRepository.save(Task.builder()
                .user(userA)
                .title("Alice Data Structures Lab")
                .taskType(TaskType.LAB)
                .deadline(baseTime.plusDays(3))
                .difficulty(3)
                .academicWeight(new BigDecimal("15.00"))
                .estimatedHours(new BigDecimal("3.0"))
                .status(TaskStatus.PENDING)
                .build());

        taskB = taskRepository.save(Task.builder()
                .user(userB)
                .title("Bob Machine Design")
                .taskType(TaskType.PROJECT)
                .deadline(baseTime.plusDays(4))
                .difficulty(4)
                .academicWeight(new BigDecimal("25.00"))
                .estimatedHours(new BigDecimal("5.0"))
                .status(TaskStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("15. Authenticated user can start a task-linked session")
    void authenticatedUser_canStartTaskLinkedSession() {
        StartSessionRequest req = StartSessionRequest.builder()
                .taskId(taskA.getId())
                .notes("Implementing red-black tree delete operation")
                .build();

        StudySessionResponse res = studySessionService.startSession(userA.getId(), req, baseTime);

        assertNotNull(res);
        assertNotNull(res.getId());
        assertEquals(taskA.getId(), res.getTaskId());
        assertEquals(userA.getId(), res.getUserId());
        assertFalse(res.getIsCompleted());
        assertEquals(0, res.getDurationMinutes());
        assertEquals("Implementing red-black tree delete operation", res.getNotes());
    }

    @Test
    @DisplayName("16. Authenticated user can start a general session without taskId")
    void authenticatedUser_canStartGeneralSession() {
        StartSessionRequest req = StartSessionRequest.builder()
                .taskId(null)
                .notes("General study block: exam review")
                .build();

        StudySessionResponse res = studySessionService.startSession(userA.getId(), req, baseTime);

        assertNotNull(res);
        assertNull(res.getTaskId());
        assertEquals(userA.getId(), res.getUserId());
        assertFalse(res.getIsCompleted());
    }

    @Test
    @DisplayName("17. Session start sets correct user")
    void sessionStart_setsCorrectUser() {
        StudySessionResponse resA = studySessionService.startSession(userA.getId(), null, baseTime);
        assertEquals(userA.getId(), resA.getUserId());
    }

    @Test
    @DisplayName("18. User cannot start a session using another user's task (returns 404)")
    void userCannotStartSessionUsingAnotherUsersTask() {
        StartSessionRequest req = StartSessionRequest.builder()
                .taskId(taskB.getId()) // Bob's task ID
                .notes("Trying to link to Bob's task")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> studySessionService.startSession(userA.getId(), req, baseTime));
    }

    @Test
    @DisplayName("19 & 20. Authenticated user can stop own session and duration is calculated correctly")
    void authenticatedUser_canStopOwnSession_andDurationIsCalculated() {
        StudySessionResponse started = studySessionService.startSession(userA.getId(), null, baseTime);

        // Advance 45 minutes
        LocalDateTime stopTime = baseTime.plusMinutes(45);
        StopSessionRequest stopReq = StopSessionRequest.builder().notes("Finished chapter 4").build();

        StudySessionResponse stopped = studySessionService.stopSession(userA.getId(), started.getId(), stopReq, stopTime);

        assertNotNull(stopped);
        assertTrue(stopped.getIsCompleted());
        assertEquals(45, stopped.getDurationMinutes());
        assertEquals(stopTime, stopped.getEndTime());
        assertEquals("Finished chapter 4", stopped.getNotes());
    }

    @Test
    @DisplayName("21. Completed session is marked completed")
    void completedSession_isMarkedCompleted() {
        StudySessionResponse started = studySessionService.startSession(userA.getId(), null, baseTime);
        StudySessionResponse stopped = studySessionService.stopSession(userA.getId(), started.getId(), null, baseTime.plusMinutes(25));

        assertTrue(stopped.getIsCompleted());
    }

    @Test
    @DisplayName("22. User cannot stop another user's session (returns 404)")
    void userCannotStopAnotherUsersSession() {
        StudySessionResponse bobSession = studySessionService.startSession(userB.getId(), null, baseTime);

        // Alice attempts to stop Bob's session
        assertThrows(ResourceNotFoundException.class,
                () -> studySessionService.stopSession(userA.getId(), bobSession.getId(), null, baseTime.plusMinutes(10)));
    }

    @Test
    @DisplayName("23. User cannot view another user's sessions")
    void userCannotViewAnotherUsersSessions() {
        studySessionService.startSession(userB.getId(), null, baseTime);

        // Alice queries her sessions -> must be 0
        var aliceSessions = studySessionService.getUserSessions(userA.getId(), null, null);
        assertTrue(aliceSessions.isEmpty());
    }

    @Test
    @DisplayName("25. Invalid session lifecycle operations return appropriate errors")
    void invalidSessionLifecycleOperations_returnAppropriateErrors() {
        StudySessionResponse session = studySessionService.startSession(userA.getId(), null, baseTime);

        // Stopping session
        studySessionService.stopSession(userA.getId(), session.getId(), null, baseTime.plusMinutes(30));

        // Attempting to stop already completed session -> 400
        assertThrows(BadRequestException.class,
                () -> studySessionService.stopSession(userA.getId(), session.getId(), null, baseTime.plusMinutes(40)));

        // Attempting to pause already completed session -> 400
        assertThrows(BadRequestException.class,
                () -> studySessionService.pauseSession(userA.getId(), session.getId()));

        // Attempting to resume already completed session -> 400
        assertThrows(BadRequestException.class,
                () -> studySessionService.resumeSession(userA.getId(), session.getId()));
    }

    @Test
    @DisplayName("26. Negative duration is never persisted")
    void negativeDuration_isNeverPersisted() {
        StudySessionResponse session = studySessionService.startSession(userA.getId(), null, baseTime);

        // End time before start time must be rejected
        LocalDateTime invalidEndTime = baseTime.minusMinutes(10);
        assertThrows(BadRequestException.class,
                () -> studySessionService.stopSession(userA.getId(), session.getId(), null, invalidEndTime));
    }

    @Test
    @DisplayName("27. Simultaneous active session is rejected (Single Active Session rule -> 409)")
    void simultaneousActiveSession_isRejected() {
        studySessionService.startSession(userA.getId(), null, baseTime);

        // Attempting to start a second session while first is active -> 409 Conflict
        assertThrows(DuplicateResourceException.class,
                () -> studySessionService.startSession(userA.getId(), null, baseTime.plusMinutes(5)));
    }

    @Test
    @DisplayName("Pause and resume correctly deducts paused time from final session duration")
    void pauseAndResume_deductsPausedTimeCorrectly() {
        // Start at 10:00
        StudySessionResponse started = studySessionService.startSession(userA.getId(), null, baseTime);

        // Pause at 10:20 (20 minutes active)
        studySessionService.pauseSession(userA.getId(), started.getId(), baseTime.plusMinutes(20));

        // Resume at 10:35 (15 minutes paused)
        studySessionService.resumeSession(userA.getId(), started.getId(), baseTime.plusMinutes(35));

        // Stop at 11:00 (25 more minutes active => total active = 20 + 25 = 45 min)
        StudySessionResponse stopped = studySessionService.stopSession(
                userA.getId(), started.getId(), null, baseTime.plusHours(1));

        assertEquals(45, stopped.getDurationMinutes());
    }
}
