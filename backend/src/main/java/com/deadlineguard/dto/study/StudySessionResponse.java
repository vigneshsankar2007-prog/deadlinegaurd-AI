package com.deadlineguard.dto.study;

import com.deadlineguard.entity.StudySession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Public response DTO exposing focus session tracking data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionResponse {

    private Long id;
    private Long userId;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Boolean isCompleted;
    private String notes;
    private LocalDateTime createdAt;

    public static StudySessionResponse fromEntity(StudySession entity) {
        if (entity == null) {
            return null;
        }
        return StudySessionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskTitle(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationMinutes(entity.getDurationMinutes())
                .isCompleted(entity.getIsCompleted())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
