package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.subject.CreateSubjectRequest;
import com.deadlineguard.dto.subject.SubjectResponse;
import com.deadlineguard.dto.subject.UpdateSubjectRequest;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for student academic subject management.
 * Enforces authenticated student ownership across all operations.
 */
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
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
     * Create a new academic subject for the authenticated student.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateSubjectRequest request
    ) {
        Long userId = getUserId(userDetails);
        SubjectResponse response = subjectService.createSubject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subject created successfully", response));
    }

    /**
     * Retrieve all academic subjects enrolled by the authenticated student.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        List<SubjectResponse> responses = subjectService.getSubjects(userId);
        return ResponseEntity.ok(ApiResponse.success("Subjects retrieved successfully", responses));
    }

    /**
     * Retrieve a single academic subject by ID (only if owned by the authenticated student).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> getSubjectById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        SubjectResponse response = subjectService.getSubjectById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject retrieved successfully", response));
    }

    /**
     * Update an academic subject (only if owned by the authenticated student).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        Long userId = getUserId(userDetails);
        SubjectResponse response = subjectService.updateSubject(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Subject updated successfully", response));
    }

    /**
     * Delete an academic subject (only if owned by the authenticated student).
     * Tasks associated with this subject are unlinked and preserved.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        subjectService.deleteSubject(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Subject deleted successfully"));
    }
}
