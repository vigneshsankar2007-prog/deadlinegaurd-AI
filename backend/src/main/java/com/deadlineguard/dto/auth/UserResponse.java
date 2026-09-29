package com.deadlineguard.dto.auth;

import com.deadlineguard.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Safe public user representation returned in API responses.
 * Strictly omits password_hash and any credential secrets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String department;
    private Integer semester;
    private String college;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .semester(user.getSemester())
                .college(user.getCollege())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
