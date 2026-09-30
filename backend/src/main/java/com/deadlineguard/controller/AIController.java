package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.ai.AIRecommendationResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Google Gemini AI student recommendations.
 * Strictly requires JWT authentication and isolates data per authenticated student.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
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
     * Primary Gemini AI Feature: "What should I do now?"
     * Generates a grounded, contextual recommendation analyzing the student's real deliverables
     * and authoritative deterministic priorities.
     */
    @PostMapping("/what-should-i-do-now")
    public ResponseEntity<ApiResponse<AIRecommendationResponse>> getWhatShouldIDoNow(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        AIRecommendationResponse response = aiService.generateWhatShouldIDoNow(userId);
        return ResponseEntity.ok(ApiResponse.success("AI recommendation generated successfully", response));
    }

    /**
     * Retrieve all historical recommendations for the authenticated student.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<AIRecommendationResponse>>> getStudentRecommendations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        List<AIRecommendationResponse> responses = aiService.getRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved successfully", responses));
    }

    /**
     * Retrieve a specific recommendation by ID (only if owned by the authenticated student).
     */
    @GetMapping("/recommendations/{id}")
    public ResponseEntity<ApiResponse<AIRecommendationResponse>> getRecommendationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        AIRecommendationResponse response = aiService.getRecommendationById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recommendation retrieved successfully", response));
    }
}
