package com.deadlineguard.entity.enums;

/**
 * Categorization of academic task deliverables.
 * Matches MySQL ENUM('ASSIGNMENT', 'PROJECT', 'EXAM', 'LAB', 'RECORD', 'PRESENTATION', 'OTHER')
 */
public enum TaskType {
    ASSIGNMENT,
    PROJECT,
    EXAM,
    LAB,
    RECORD,
    PRESENTATION,
    OTHER
}
