package com.deadlineguard.dto.subject;

import com.deadlineguard.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Public response DTO for Subject data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String subjectName;
    private String subjectCode;
    private Integer credits;
    private String colorHex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubjectResponse fromEntity(Subject subject) {
        if (subject == null) {
            return null;
        }
        return SubjectResponse.builder()
                .id(subject.getId())
                .subjectName(subject.getSubjectName())
                .subjectCode(subject.getSubjectCode())
                .credits(subject.getCredits())
                .colorHex(subject.getColorHex())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }
}
