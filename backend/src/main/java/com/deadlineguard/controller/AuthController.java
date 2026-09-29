package com.deadlineguard.controller;

import com.deadlineguard.common.ApiResponse;
import com.deadlineguard.dto.auth.AuthResponse;
import com.deadlineguard.dto.auth.LoginRequest;
import com.deadlineguard.dto.auth.RegisterRequest;
import com.deadlineguard.dto.auth.UserResponse;
import com.deadlineguard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user registration, authentication, and current authenticated user retrieval.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new student account and returns a JWT access token.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student registration successful", response));
    }

    /**
     * Authenticates existing student credentials and returns a JWT access token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Retrieves the profile of the currently authenticated user from SecurityContext.
     * Requires valid Bearer JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }
}
