-- =============================================================================
-- DEADLINEGUARD AI - DATABASE SCHEMA (FINAL FROZEN SPECIFICATION)
-- Target Engine: MySQL 8.0+
-- Storage Engine: InnoDB
-- Character Encoding: UTF8MB4 (Full Unicode & Emoji Support)
-- Collation: utf8mb4_unicode_ci
-- Compatibility: Java 21, Spring Boot 3.3.x, Spring Data JPA / Hibernate 6.x
-- Description: Intelligent Student Task & Deadline Management System
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS productivity_stats;
DROP TABLE IF EXISTS ai_recommendations;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS study_sessions;
DROP TABLE IF EXISTS task_priorities;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. TABLE: users
-- Purpose: Student accounts, university profile, and authentication credentials.
-- JPA Mapping: com.deadlineguard.entity.User
-- =============================================================================
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Full name of student',
    email VARCHAR(150) NOT NULL COMMENT 'Unique student university or personal email',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt-compatible password hash ($2a$ or $2b$, 60-char payload)',
    department VARCHAR(100) NOT NULL COMMENT 'Major or department (e.g., Computer Science & Engineering)',
    semester INT UNSIGNED NOT NULL COMMENT 'Academic semester number (1 to 12)',
    college VARCHAR(150) NOT NULL COMMENT 'Educational institution or university name',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Profile update timestamp',

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_semester CHECK (semester BETWEEN 1 AND 12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Student accounts and authentication';

-- =============================================================================
-- 2. TABLE: subjects
-- Purpose: Academic courses with credit weighting and visual identifiers.
-- JPA Mapping: com.deadlineguard.entity.Subject
-- =============================================================================
CREATE TABLE subjects (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Owner student ID',
    subject_name VARCHAR(120) NOT NULL COMMENT 'Course title (e.g. Operating Systems)',
    subject_code VARCHAR(20) NOT NULL COMMENT 'Course catalog code (e.g. CS602)',
    credits INT UNSIGNED NOT NULL DEFAULT 3 COMMENT 'Credit hours (1 to 10)',
    color_hex VARCHAR(7) NOT NULL DEFAULT '#2563EB' COMMENT 'Hex color code for UI cards and badges',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Modification timestamp',

    CONSTRAINT fk_subjects_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_subject_code UNIQUE (user_id, subject_code),
    CONSTRAINT chk_subjects_credits CHECK (credits BETWEEN 1 AND 10),
    CONSTRAINT chk_subjects_color CHECK (color_hex REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Course subjects per student';

-- =============================================================================
-- 3. TABLE: tasks
-- Purpose: Academic deliverables (assignments, projects, exams, labs, records).
-- Integrity:
--   1. subject_id is NULLABLE (general tasks without subjects are supported).
--   2. fk_tasks_subject uses ON DELETE SET NULL: deleting a subject preserves
--      tasks and safely unlinks them without losing student work.
--   3. Subject ownership is validated by the backend service layer.
--      Database-level FK preserves referential integrity while ON DELETE SET NULL
--      preserves tasks when subjects are deleted.
--   4. chk_tasks_completion strictly enforces completion timestamp integrity.
-- JPA Mapping: com.deadlineguard.entity.Task
-- =============================================================================
CREATE TABLE tasks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Owner student ID',
    subject_id BIGINT UNSIGNED NULL COMMENT 'Associated course ID (NULL if general/extracurricular task)',
    title VARCHAR(200) NOT NULL COMMENT 'Task title / deliverable name',
    description TEXT NULL COMMENT 'Detailed submission guidelines, rubrics, and instructions',
    task_type ENUM('ASSIGNMENT', 'PROJECT', 'EXAM', 'LAB', 'RECORD', 'PRESENTATION', 'OTHER') NOT NULL DEFAULT 'ASSIGNMENT',
    deadline DATETIME NOT NULL COMMENT 'Due date and time',
    difficulty TINYINT UNSIGNED NOT NULL COMMENT 'Difficulty: 1=Very Easy, 2=Easy, 3=Moderate, 4=Difficult, 5=Very Difficult',
    academic_weight DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT 'Percentage weight towards course/semester grade (0.00 to 100.00)',
    estimated_hours DECIMAL(4,1) NOT NULL DEFAULT 1.0 COMMENT 'Estimated effort in hours (must be > 0.0)',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE') NOT NULL DEFAULT 'PENDING',
    completed_at DATETIME NULL COMMENT 'Timestamp when task was marked COMPLETED (NULL while incomplete)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Modification timestamp',

    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_subject FOREIGN KEY (subject_id) 
        REFERENCES subjects (id) ON DELETE SET NULL,
    CONSTRAINT chk_tasks_difficulty CHECK (difficulty BETWEEN 1 AND 5),
    CONSTRAINT chk_tasks_weight CHECK (academic_weight >= 0.00 AND academic_weight <= 100.00),
    CONSTRAINT chk_tasks_hours CHECK (estimated_hours > 0.0),
    CONSTRAINT chk_tasks_completion CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL) OR
        (status != 'COMPLETED' AND completed_at IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Academic assignments, exams, and deliverables';

-- =============================================================================
-- 4. TABLE: task_priorities
-- Purpose: Stores calculated 5-factor explainable priority score for each task.
--
-- FORMULA:
--   priority_score = 0.40 * urgency_score 
--                  + 0.20 * difficulty_score 
--                  + 0.20 * weight_score 
--                  + 0.10 * effort_score 
--                  + 0.10 * workload_score
--
-- NORMALIZATION:
--   Composite score is normalized to 0.00 - 100.00.
--   All 5 component subscores are normalized to 0.00 - 100.00.
--
-- PRIORITY TIERS:
--   76.00 - 100.00 = CRITICAL
--   51.00 -  75.99 = HIGH
--   31.00 -  50.99 = MEDIUM
--    0.00 -  30.99 = LOW
--
-- JPA Mapping: com.deadlineguard.entity.TaskPriority
-- =============================================================================
CREATE TABLE task_priorities (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT UNSIGNED NOT NULL COMMENT 'One-to-one linked task ID',
    priority_score DECIMAL(5,2) NOT NULL COMMENT 'Composite score (0.00 - 100.00)',
    priority_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL COMMENT 'Categorical priority level',
    urgency_score DECIMAL(5,2) NOT NULL COMMENT 'Deadline urgency subscore (0.00 - 100.00)',
    difficulty_score DECIMAL(5,2) NOT NULL COMMENT 'Difficulty subscore (0.00 - 100.00)',
    weight_score DECIMAL(5,2) NOT NULL COMMENT 'Academic weight subscore (0.00 - 100.00)',
    effort_score DECIMAL(5,2) NOT NULL COMMENT 'Estimated effort subscore (0.00 - 100.00)',
    workload_score DECIMAL(5,2) NOT NULL COMMENT 'Concurrent 72h workload subscore (0.00 - 100.00)',
    explanation_text TEXT NOT NULL COMMENT 'Explainable human-readable breakdown of the score',
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of dynamic calculation',

    CONSTRAINT uq_priorities_task UNIQUE (task_id),
    CONSTRAINT fk_priorities_task FOREIGN KEY (task_id) 
        REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT chk_priority_score CHECK (priority_score >= 0.00 AND priority_score <= 100.00),
    CONSTRAINT chk_urgency_score CHECK (urgency_score >= 0.00 AND urgency_score <= 100.00),
    CONSTRAINT chk_diff_score CHECK (difficulty_score >= 0.00 AND difficulty_score <= 100.00),
    CONSTRAINT chk_weight_score CHECK (weight_score >= 0.00 AND weight_score <= 100.00),
    CONSTRAINT chk_effort_score CHECK (effort_score >= 0.00 AND effort_score <= 100.00),
    CONSTRAINT chk_workload_score CHECK (workload_score >= 0.00 AND workload_score <= 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dynamic 5-factor explainable priority calculations';

-- =============================================================================
-- 5. TABLE: study_sessions
-- Purpose: Tracks focus mode and Pomodoro sessions (can be tied to a task or general).
-- JPA Mapping: com.deadlineguard.entity.StudySession
-- =============================================================================
CREATE TABLE study_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student who completed the session',
    task_id BIGINT UNSIGNED NULL COMMENT 'Associated task ID (NULL for general study sessions)',
    start_time DATETIME NOT NULL COMMENT 'Session start timestamp',
    end_time DATETIME NULL COMMENT 'Session end timestamp (NULL while currently active)',
    duration_minutes INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total focus duration in minutes',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'TRUE if session finished; FALSE if active/abandoned',
    notes VARCHAR(255) NULL COMMENT 'Student reflection or progress notes',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',

    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_task FOREIGN KEY (task_id) 
        REFERENCES tasks (id) ON DELETE SET NULL,
    CONSTRAINT chk_sessions_duration CHECK (duration_minutes >= 0),
    CONSTRAINT chk_sessions_time CHECK (end_time IS NULL OR end_time >= start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Study session focus timer logs';

-- =============================================================================
-- 6. TABLE: notifications
-- Purpose: Scheduled deadline reminders (48h, 24h, 6h) and urgency alerts.
-- JPA Mapping: com.deadlineguard.entity.Notification
-- =============================================================================
CREATE TABLE notifications (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Recipient student ID',
    task_id BIGINT UNSIGNED NULL COMMENT 'Associated task ID (NULL for system/daily summary alerts)',
    title VARCHAR(120) NOT NULL COMMENT 'Notification headline',
    message TEXT NOT NULL COMMENT 'Actionable alert message',
    notification_type ENUM('DEADLINE_48H', 'DEADLINE_24H', 'DEADLINE_6H', 'OVERDUE', 'HIGH_PRIORITY', 'DAILY_SUMMARY') NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Read status flag',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Alert trigger timestamp',

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_task FOREIGN KEY (task_id) 
        REFERENCES tasks (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Automated deadline reminders and notification alerts';

-- =============================================================================
-- 7. TABLE: ai_recommendations
-- Purpose: Contextual recommendations and study schedules generated via Gemini AI.
--          Stored separately from the deterministic priority engine.
-- JPA Mapping: com.deadlineguard.entity.AIRecommendation
-- =============================================================================
CREATE TABLE ai_recommendations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Target student ID',
    task_id BIGINT UNSIGNED NULL COMMENT 'Associated task ID (NULL for multi-task study schedules)',
    recommendation_type VARCHAR(50) NOT NULL COMMENT 'E.g., WHAT_SHOULD_I_DO_NOW, STUDY_PLAN, TASK_EXPLANATION',
    recommendation_text TEXT NOT NULL COMMENT 'Headline recommendation statement',
    ai_reasoning TEXT NOT NULL COMMENT 'Detailed contextual reasoning grounded in actual academic data',
    suggested_action VARCHAR(255) NOT NULL COMMENT 'Specific next operational step',
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Generation timestamp',

    CONSTRAINT fk_ai_rec_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_rec_task FOREIGN KEY (task_id) 
        REFERENCES tasks (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Gemini AI contextual recommendations and study plans';

-- =============================================================================
-- 8. TABLE: productivity_stats
-- Purpose: Pre-aggregated daily student productivity metrics for analytics charts.
-- JPA Mapping: com.deadlineguard.entity.ProductivityStat
-- =============================================================================
CREATE TABLE productivity_stats (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Student ID',
    stat_date DATE NOT NULL COMMENT 'Calendar date for metrics aggregation',
    tasks_completed INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Number of tasks finished on this date',
    tasks_overdue INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Number of tasks overdue on this date',
    study_minutes INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total focus minutes recorded on this date',
    productivity_score DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Daily composite completion ratio (0.00 - 100.00)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Row creation timestamp',

    CONSTRAINT fk_stats_user FOREIGN KEY (user_id) 
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_stat_date UNIQUE (user_id, stat_date),
    CONSTRAINT chk_productivity_score CHECK (productivity_score >= 0.00 AND productivity_score <= 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily student productivity summaries for analytics';

-- =============================================================================
-- PERFORMANCE INDEXES (NON-REDUNDANT)
-- =============================================================================

-- Tasks Table:
-- Note: (user_id) alone is covered by idx_tasks_user_status_deadline prefix.
CREATE INDEX idx_tasks_subject ON tasks (subject_id);
CREATE INDEX idx_tasks_deadline ON tasks (deadline);
CREATE INDEX idx_tasks_user_status_deadline ON tasks (user_id, status, deadline);

-- Task Priorities:
-- Note: task_id is already indexed by uq_priorities_task.
CREATE INDEX idx_priorities_score_level ON task_priorities (priority_score, priority_level);

-- Study Sessions:
-- Note: user_id is covered by idx_sessions_user_start prefix.
CREATE INDEX idx_sessions_user_start ON study_sessions (user_id, start_time);
CREATE INDEX idx_sessions_task ON study_sessions (task_id);

-- Notifications:
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read, created_at);
CREATE INDEX idx_notifications_task ON notifications (task_id);

-- AI Recommendations:
CREATE INDEX idx_ai_recs_user_date ON ai_recommendations (user_id, generated_at);
CREATE INDEX idx_ai_recs_task ON ai_recommendations (task_id);

-- Productivity Stats:
-- Note: (user_id, stat_date) is already indexed by uq_user_stat_date.
