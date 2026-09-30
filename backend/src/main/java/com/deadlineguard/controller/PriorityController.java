package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.priority.PriorityResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.PriorityCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for deterministic 5-factor task priority calculation and breakdown.
 * Strictly requires JWT authentication and enforces authenticated student ownership.
 */
@RestController
@RequiredArgsConstructor
public class PriorityController {

    private final PriorityCalculationService priorityCalculationService;
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
     * Retrieve the calculated priority for an owned task.
     * Endpoint: GET /api/v1/tasks/{taskId}/priority
     */
    @GetMapping("/api/v1/tasks/{taskId}/priority")
    public ResponseEntity<ApiResponse<PriorityResponse>> getTaskPriority(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId
    ) {
        Long userId = getUserId(userDetails);
        PriorityResponse response = priorityCalculationService.getTaskPriority(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task priority retrieved successfully", response));
    }

    /**
     * Recalculate and persist the priority for an owned task.
     * Endpoint: POST /api/v1/tasks/{taskId}/priority/recalculate
     */
    @PostMapping("/api/v1/tasks/{taskId}/priority/recalculate")
    public ResponseEntity<ApiResponse<PriorityResponse>> recalculateTaskPriority(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId
    ) {
        Long userId = getUserId(userDetails);
        PriorityResponse response = priorityCalculationService.calculateAndPersistPriority(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task priority recalculated successfully", response));
    }

    /**
     * Retrieve all task priorities belonging to the authenticated student.
     * Endpoint: GET /api/v1/priorities
     * Optional sorting: sortBy=score (default, highest first) or sortBy=deadline (earliest first)
     */
    @GetMapping("/api/v1/priorities")
    public ResponseEntity<ApiResponse<List<PriorityResponse>>> getStudentPriorities(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "score") String sortBy
    ) {
        Long userId = getUserId(userDetails);
        List<PriorityResponse> responses = priorityCalculationService.getUserTaskPriorities(userId, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Priorities retrieved successfully", responses));
    }
}
