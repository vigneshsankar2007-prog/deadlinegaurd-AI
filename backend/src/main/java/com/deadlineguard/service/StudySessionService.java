package com.deadlineguard.service;

import com.deadlineguard.dto.study.StartSessionRequest;
import com.deadlineguard.dto.study.StopSessionRequest;
import com.deadlineguard.dto.study.StudySessionResponse;
import com.deadlineguard.entity.StudySession;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.BadRequestException;
import com.deadlineguard.exception.DuplicateResourceException;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.StudySessionRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service managing Focus Timer and Study Session lifecycle:
 * start, pause, resume, stop, and server-side duration calculation.
 *
 * ARCHITECTURAL CONTRACT:
 * - Single Active Session Rule: Enforces that a student cannot have multiple concurrent active sessions (409 Conflict).
 * - Multi-tenant isolation: All lookups and mutations require authenticated student ownership.
 * - Server-authoritative duration: Duration is calculated server-side and never trusted from the client.
 * - Negative duration prevention: Durations are strictly clamped to >= 0 minutes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Runtime state tracking for active session pauses without schema mutation
    private final Map<Long, PauseTracker> pauseTrackers = new ConcurrentHashMap<>();

    private static class PauseTracker {
        boolean paused = false;
        LocalDateTime pausedAt;
        long totalPausedSeconds = 0;
    }

    /**
     * Start a new focus timer session (task-linked or general).
     * Enforces the single active session rule.
     */
    @Transactional
    public StudySessionResponse startSession(Long userId, StartSessionRequest request, LocalDateTime fixedStartTime) {
        log.info("Starting study session for userId: {}, taskId: {}", userId, request != null ? request.getTaskId() : null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Enforce single active session rule
        Optional<StudySession> activeSession = studySessionRepository.findByUserIdAndIsCompletedFalse(userId);
        if (activeSession.isPresent()) {
            log.warn("Conflict: userId: {} already has active session id: {}", userId, activeSession.get().getId());
            throw new DuplicateResourceException(
                    "You already have an active study session in progress (Session #" + activeSession.get().getId() + "). "
                    + "Please stop it before starting a new session."
            );
        }

        Task task = null;
        if (request != null && request.getTaskId() != null) {
            task = taskRepository.findByIdAndUserId(request.getTaskId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.getTaskId()));
        }

        LocalDateTime startTime = fixedStartTime != null ? fixedStartTime : LocalDateTime.now();

        StudySession session = StudySession.builder()
                .user(user)
                .task(task)
                .startTime(startTime)
                .durationMinutes(0)
                .isCompleted(false)
                .notes(request != null ? request.getNotes() : null)
                .build();

        StudySession saved = studySessionRepository.save(session);
        pauseTrackers.put(saved.getId(), new PauseTracker());

        log.info("Started study session id: {} for userId: {}", saved.getId(), userId);
        return StudySessionResponse.fromEntity(saved);
    }

    @Transactional
    public StudySessionResponse startSession(Long userId, StartSessionRequest request) {
        return startSession(userId, request, null);
    }

    /**
     * Stop and complete an active focus session.
     * Computes server-side duration accounting for pause periods.
     */
    @Transactional
    public StudySessionResponse stopSession(Long userId, Long sessionId, StopSessionRequest request, LocalDateTime fixedEndTime) {
        log.info("Stopping study session id: {} for userId: {}", sessionId, userId);

        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudySession", "id", sessionId));

        if (Boolean.TRUE.equals(session.getIsCompleted())) {
            throw new BadRequestException("Study session #" + sessionId + " is already stopped and completed.");
        }

        LocalDateTime endTime = fixedEndTime != null ? fixedEndTime : LocalDateTime.now();

        if (endTime.isBefore(session.getStartTime())) {
            throw new BadRequestException("Session end time cannot be before start time.");
        }

        PauseTracker tracker = pauseTrackers.remove(sessionId);
        long pausedSeconds = 0;
        if (tracker != null) {
            pausedSeconds = tracker.totalPausedSeconds;
            if (tracker.paused && tracker.pausedAt != null) {
                pausedSeconds += Duration.between(tracker.pausedAt, endTime).toSeconds();
            }
        }

        long totalSeconds = Duration.between(session.getStartTime(), endTime).toSeconds();
        long activeSeconds = Math.max(0, totalSeconds - pausedSeconds);
        int finalMinutes = (int) Math.max(0, activeSeconds / 60);

        session.setEndTime(endTime);
        session.setDurationMinutes(finalMinutes);
        session.setIsCompleted(true);

        if (request != null && request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            session.setNotes(request.getNotes().trim());
        }

        StudySession saved = studySessionRepository.save(session);
        log.info("Stopped study session id: {} with final duration: {} minutes", saved.getId(), finalMinutes);
        return StudySessionResponse.fromEntity(saved);
    }

    @Transactional
    public StudySessionResponse stopSession(Long userId, Long sessionId, StopSessionRequest request) {
        return stopSession(userId, sessionId, request, null);
    }

    /**
     * Pause an active study session.
     */
    @Transactional
    public StudySessionResponse pauseSession(Long userId, Long sessionId, LocalDateTime fixedPauseTime) {
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudySession", "id", sessionId));

        if (Boolean.TRUE.equals(session.getIsCompleted())) {
            throw new BadRequestException("Cannot pause a completed study session.");
        }

        PauseTracker tracker = pauseTrackers.computeIfAbsent(sessionId, k -> new PauseTracker());
        if (tracker.paused) {
            throw new BadRequestException("Study session #" + sessionId + " is already paused.");
        }

        tracker.paused = true;
        tracker.pausedAt = fixedPauseTime != null ? fixedPauseTime : LocalDateTime.now();
        log.info("Paused study session id: {} at {}", sessionId, tracker.pausedAt);

        return StudySessionResponse.fromEntity(session);
    }

    @Transactional
    public StudySessionResponse pauseSession(Long userId, Long sessionId) {
        return pauseSession(userId, sessionId, null);
    }

    /**
     * Resume a paused study session.
     */
    @Transactional
    public StudySessionResponse resumeSession(Long userId, Long sessionId, LocalDateTime fixedResumeTime) {
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudySession", "id", sessionId));

        if (Boolean.TRUE.equals(session.getIsCompleted())) {
            throw new BadRequestException("Cannot resume a completed study session.");
        }

        PauseTracker tracker = pauseTrackers.get(sessionId);
        if (tracker == null || !tracker.paused) {
            throw new BadRequestException("Study session #" + sessionId + " is not currently paused.");
        }

        LocalDateTime resumeTime = fixedResumeTime != null ? fixedResumeTime : LocalDateTime.now();
        if (tracker.pausedAt != null) {
            long pauseDuration = Math.max(0, Duration.between(tracker.pausedAt, resumeTime).toSeconds());
            tracker.totalPausedSeconds += pauseDuration;
        }

        tracker.paused = false;
        tracker.pausedAt = null;
        log.info("Resumed study session id: {} at {}", sessionId, resumeTime);

        return StudySessionResponse.fromEntity(session);
    }

    @Transactional
    public StudySessionResponse resumeSession(Long userId, Long sessionId) {
        return resumeSession(userId, sessionId, null);
    }

    /**
     * Retrieve currently active (uncompleted) session for the student.
     */
    @Transactional(readOnly = true)
    public Optional<StudySessionResponse> getActiveSession(Long userId) {
        return studySessionRepository.findByUserIdAndIsCompletedFalse(userId)
                .map(StudySessionResponse::fromEntity);
    }

    /**
     * Retrieve study sessions for the authenticated student with optional filters.
     */
    @Transactional(readOnly = true)
    public List<StudySessionResponse> getUserSessions(Long userId, Long taskId, Boolean completed) {
        List<StudySession> sessions;
        if (taskId != null) {
            sessions = studySessionRepository.findByUserIdAndTaskIdOrderByStartTimeDesc(userId, taskId);
        } else if (completed != null) {
            sessions = studySessionRepository.findByUserIdAndIsCompletedOrderByStartTimeDesc(userId, completed);
        } else {
            sessions = studySessionRepository.findByUserIdOrderByStartTimeDesc(userId);
        }

        return sessions.stream()
                .map(StudySessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific study session only if owned by the student.
     */
    @Transactional(readOnly = true)
    public StudySessionResponse getSessionById(Long userId, Long sessionId) {
        return studySessionRepository.findByIdAndUserId(sessionId, userId)
                .map(StudySessionResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("StudySession", "id", sessionId));
    }
}
