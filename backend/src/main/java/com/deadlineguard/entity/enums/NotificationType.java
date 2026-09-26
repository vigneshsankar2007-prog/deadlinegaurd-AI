package com.deadlineguard.entity.enums;

/**
 * Triggers and categories for student deadline reminders and system alerts.
 * Matches MySQL ENUM('DEADLINE_48H', 'DEADLINE_24H', 'DEADLINE_6H', 'OVERDUE', 'HIGH_PRIORITY', 'DAILY_SUMMARY')
 */
public enum NotificationType {
    DEADLINE_48H,
    DEADLINE_24H,
    DEADLINE_6H,
    OVERDUE,
    HIGH_PRIORITY,
    DAILY_SUMMARY
}
