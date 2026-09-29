package com.deadlineguard.service;

import com.deadlineguard.dto.auth.AuthResponse;
import com.deadlineguard.dto.auth.LoginRequest;
import com.deadlineguard.dto.auth.RegisterRequest;
import com.deadlineguard.dto.auth.UserResponse;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.BadRequestException;
import com.deadlineguard.exception.DuplicateResourceException;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service orchestrating student registration, BCrypt credential verification, and JWT issuance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new student account, hashes the password via BCrypt, and issues an initial JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment().trim())
                .semester(request.getSemester())
                .college(request.getCollege().trim())
                .build();

        final User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database unique constraint caught race-condition registration for email: {}", request.getEmail());
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        String token = jwtService.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(savedUser))
                .build();
    }

    /**
     * Verifies student credentials against stored BCrypt hash and issues JWT access token.
     * Always returns generic 'Invalid email or password' message on mismatch to prevent enumeration.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Processing login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password verification failed for email: {}", email);
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }

    /**
     * Retrieves the profile of the currently authenticated student.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.fromEntity(user);
    }
}
