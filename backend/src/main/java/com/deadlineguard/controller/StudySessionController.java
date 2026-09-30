package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.study.StartSessionRequest;
import com.deadlineguard.dto.study.StopSessionRequest;
import com.deadlineguard.dto.study.StudySessionResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Focus Timer and Study Session lifecycle management.
 * Strictly requires JWT authentication and isolates data per authenticated student.
 */
@RestController
@RequestMapping("/api/v1/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    /**
     * Start a new focus study session (task-linked or general).
     * Endpoint: POST /api/v1/study-sessions/start
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StudySessionResponse>> startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody(required = false) StartSessionRequest request
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.startSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Study session started successfully", response));
    }

    /**
     * Stop and complete an active focus session.
     * Endpoint: POST /api/v1/study-sessions/{id}/stop
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<StudySessionResponse>> stopSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) StopSessionRequest request
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.stopSession(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Study session completed successfully", response));
    }

    /**
     * Pause an active focus session.
     * Endpoint: POST /api/v1/study-sessions/{id}/pause
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<StudySessionResponse>> pauseSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.pauseSession(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Study session paused successfully", response));
    }

    /**
     * Resume a paused focus session.
     * Endpoint: POST /api/v1/study-sessions/{id}/resume
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<StudySessionResponse>> resumeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.resumeSession(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Study session resumed successfully", response));
    }

    /**
     * Retrieve currently active session for the authenticated student.
     * Endpoint: GET /api/v1/study-sessions/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<StudySessionResponse>> getActiveSession(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.getActiveSession(userId).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Active study session retrieved", response));
    }

    /**
     * Retrieve all study sessions for the authenticated student with optional filters.
     * Endpoint: GET /api/v1/study-sessions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySessionResponse>>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Boolean completed
    ) {
        Long userId = getUserId(userDetails);
        List<StudySessionResponse> responses = studySessionService.getUserSessions(userId, taskId, completed);
        return ResponseEntity.ok(ApiResponse.success("Study sessions retrieved successfully", responses));
    }

    /**
     * Retrieve a specific study session by ID (only if owned by the student).
     * Endpoint: GET /api/v1/study-sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudySessionResponse>> getSessionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        StudySessionResponse response = studySessionService.getSessionById(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Study session retrieved successfully", response));
    }
}
