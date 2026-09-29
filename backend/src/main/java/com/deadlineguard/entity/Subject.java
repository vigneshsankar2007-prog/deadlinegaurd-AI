package com.deadlineguard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an academic course/subject enrolled by a student.
 * Mapped to table `subjects`.
 * Unique per student: (user_id, subject_code).
 */
@Entity
@Table(
    name = "subjects",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_subject_code", columnNames = {"user_id", "subject_code"})
    },
    indexes = {
        @Index(name = "idx_subjects_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Owner student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @NotBlank(message = "Subject name is required")
    @Size(max = 120, message = "Subject name must not exceed 120 characters")
    @Column(name = "subject_name", nullable = false, length = 120)
    private String subjectName;

    @NotBlank(message = "Subject catalog code is required")
    @Size(max = 20, message = "Subject code must not exceed 20 characters")
    @Column(name = "subject_code", nullable = false, length = 20)
    private String subjectCode;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    @Column(name = "credits", nullable = false)
    @Builder.Default
    private Integer credits = 3;

    @NotBlank(message = "Color hex code is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid 6-character hex code (e.g. #2563EB)")
    @Column(name = "color_hex", nullable = false, length = 7)
    @Builder.Default
    private String colorHex = "#2563EB";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
