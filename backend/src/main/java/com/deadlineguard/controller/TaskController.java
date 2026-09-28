package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.task.CreateTaskRequest;
import com.deadlineguard.dto.task.TaskResponse;
import com.deadlineguard.dto.task.UpdateTaskRequest;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for student academic deliverable and task management.
 * Enforces authenticated student ownership across all operations.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
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
     * Create a new task for the authenticated student.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        Long userId = getUserId(userDetails);
        TaskResponse response = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    /**
     * Retrieve all tasks belonging to the authenticated student,
     * with optional status and subject filtering.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long subjectId
    ) {
        Long userId = getUserId(userDetails);
        List<TaskResponse> responses = taskService.getTasks(userId, status, subjectId);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", responses));
    }

    /**
     * Retrieve a single task by ID (only if owned by the authenticated student).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        TaskResponse response = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    /**
     * Update a task (only if owned by the authenticated student).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        Long userId = getUserId(userDetails);
        TaskResponse response = taskService.updateTask(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    /**
     * Delete a task (only if owned by the authenticated student).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        taskService.deleteTask(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }
}
