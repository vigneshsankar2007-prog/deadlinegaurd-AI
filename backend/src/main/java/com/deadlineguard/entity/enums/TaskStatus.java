package com.deadlineguard.entity.enums;

/**
 * Lifecycle status of academic tasks.
 * Matches MySQL ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE')
 */
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}
