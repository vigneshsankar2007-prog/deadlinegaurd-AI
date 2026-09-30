package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.notification.NotificationResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.CustomUserDetails;
import com.deadlineguard.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Student Deadline Reminders & Notification Management.
 * Strictly requires JWT authentication and isolates data per authenticated student.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
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
     * Retrieve notifications for the authenticated student.
     * Optional filter: ?unreadOnly=true
     * Endpoint: GET /api/v1/notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean unreadOnly
    ) {
        Long userId = getUserId(userDetails);
        List<NotificationResponse> responses = notificationService.getUserNotifications(userId, unreadOnly);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", responses));
    }

    /**
     * Shortcut endpoint to retrieve unread notifications.
     * Endpoint: GET /api/v1/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        List<NotificationResponse> responses = notificationService.getUserNotifications(userId, true);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", responses));
    }

    /**
     * Retrieve a specific notification by ID (only if owned by the student).
     * Endpoint: GET /api/v1/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        NotificationResponse response = notificationService.getNotificationById(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", response));
    }

    /**
     * Mark a single notification as read.
     * Endpoint: PATCH /api/v1/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = getUserId(userDetails);
        NotificationResponse response = notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    /**
     * Mark all notifications as read for the authenticated student.
     * Endpoint: PATCH /api/v1/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getUserId(userDetails);
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(
                "All notifications marked as read",
                Map.of("markedCount", count)
        ));
    }
}
