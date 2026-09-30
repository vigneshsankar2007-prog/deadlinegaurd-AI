package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.analytics.AnalyticsDashboardResponse;
import com.deadlineguard.dto.analytics.SubjectAnalyticsResponse;
import com.deadlineguard.dto.analytics.WeeklyAnalyticsResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Student Productivity Analytics, Trends, and Workload Insights.
 * Strictly requires JWT authentication and isolates data per authenticated student.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
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
     * Return comprehensive productivity overview for the student dashboard.
     * Endpoint: GET /api/v1/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        AnalyticsDashboardResponse response = analyticsService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success("Analytics dashboard retrieved successfully", response));
    }

    /**
     * Return 7-day chronological productivity metrics and trends.
     * Endpoint: GET /api/v1/analytics/weekly
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyAnalyticsResponse>> getWeeklyAnalytics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        WeeklyAnalyticsResponse response = analyticsService.getWeeklyAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success("Weekly analytics retrieved successfully", response));
    }

    /**
     * Return subject-wise workload and focus study breakdowns.
     * Endpoint: GET /api/v1/analytics/subjects
     */
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectAnalyticsResponse>>> getSubjectAnalytics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        List<SubjectAnalyticsResponse> responses = analyticsService.getSubjectAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success("Subject analytics retrieved successfully", responses));
    }
}
