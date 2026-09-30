package com.deadlineguard.dto.study;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Structured output JSON schema mapping for Gemini AI Study Plan generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiStudyPlanSchema {

    private String planDate;
    private List<SessionBlockSchema> sessions;
    private Integer totalStudyMinutes;
    private Integer totalBreakMinutes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionBlockSchema {
        private Long taskId;
        private String title;
        private String subjectCode;
        private String startTime;
        private String endTime;
        private Integer durationMinutes;
        private String reason;
    }
}
