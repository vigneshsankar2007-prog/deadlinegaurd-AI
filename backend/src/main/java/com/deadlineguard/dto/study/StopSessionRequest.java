package com.deadlineguard.dto.study;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload to stop/complete an active focus session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopSessionRequest {

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}
