package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.study.GenerateStudyPlanRequest;
import com.deadlineguard.dto.study.StudyPlanResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for generating AI-powered, time-blocked study schedules.
 * Strictly requires JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
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
     * Primary Stage 8 Feature: Generate AI Study Plan.
     * Generates a time-blocked study schedule grounded in student deliverables and Stage 6 priority scores.
     * Endpoint: POST /api/v1/ai/generate-study-plan
     */
    @PostMapping("/generate-study-plan")
    public ResponseEntity<ApiResponse<StudyPlanResponse>> generateStudyPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GenerateStudyPlanRequest request
    ) {
        Long userId = getUserId(userDetails);
        StudyPlanResponse response = studyPlanService.generateStudyPlan(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Study plan generated successfully", response));
    }
}
