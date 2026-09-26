package com.deadlineguard.entity.enums;

/**
 * Categorical priority level computed by the 5-factor priority engine.
 * Thresholds:
 *   CRITICAL: 76.00 - 100.00
 *   HIGH:     51.00 - 75.99
 *   MEDIUM:   31.00 - 50.99
 *   LOW:       0.00 - 30.99
 * Matches MySQL ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
 */
public enum PriorityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
