package com.deadlineguard.dto.study;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload to start a new focus session.
 * taskId is optional (allows both task-linked and general study sessions).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionRequest {

    /**
     * Optional task ID. If supplied, must belong strictly to the authenticated student.
     */
    private Long taskId;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}
