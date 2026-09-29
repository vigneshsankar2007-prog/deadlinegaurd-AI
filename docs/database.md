# DeadlineGuard AI — Database Specification

## 1. Relational Entity-Relationship Diagram (ERD)

```
       +-----------------------+
       |         users         |
       +-----------------------+
       | PK  id                |
       |     name              |
       |     email (UQ)        |
       |     password_hash     |
       |     department        |
       |     semester          |
       |     college           |
       |     created_at        |
       |     updated_at        |
       +-----------+-----------+
                   |
         +---------+---------+------------------+------------------+
         | 1:N               | 1:N              | 1:N              | 1:N
         v                   v                  v                  v
+------------------+  +--------------+  +------------------+  +---------------------+
|     subjects     |  |notifications |  |  study_sessions  |  | productivity_stats  |
+------------------+  +--------------+  +------------------+  +---------------------+
| PK  id           |  | PK  id       |  | PK  id           |  | PK  id              |
| FK  user_id      |  | FK  user_id  |  | FK  user_id      |  | FK  user_id         |
|     subject_name |  |     title    |  | FK  task_id (opt)|  |     stat_date       |
|     subject_code |  |     message  |  |     start_time   |  |     tasks_completed |
|     credits      |  |     type     |  |     end_time     |  |     tasks_overdue   |
|     color_hex    |  |     is_read  |  |     duration_mins|  |     study_mins      |
|     created_at   |  |     created_at| |     is_completed |  |     productivity_pct|
+--------+---------+  +--------------+  +------------------+  +---------------------+
         | 1:N
         v
+---------------------------------------+
|                 tasks                 |
+---------------------------------------+
| PK  id                                |
| FK  user_id                           |
| FK  subject_id                        |
|     title                             |
|     description                       |
|     task_type (ASSIGNMENT, EXAM, etc) |
|     deadline                          |
|     difficulty (1-5)                  |
|     academic_weight (0-100%)          |
|     estimated_hours                   |
|     status (PENDING, COMPLETED, etc)  |
|     completed_at                      |
|     created_at                        |
|     updated_at                        |
+-------------------+-------------------+
                    | 1:1
                    +------------------------------------+
                    |                                    | 1:N
                    v                                    v
+---------------------------------------+  +------------------------------------+
|            task_priorities            |  |         ai_recommendations         |
+---------------------------------------+  +------------------------------------+
| PK  id                                |  | PK  id                             |
| FK  task_id (UQ)                      |  | FK  task_id                        |
|     priority_score (0-100)            |  | FK  user_id                        |
|     priority_level (LOW/MED/HIGH/CRIT)|  |     recommendation_type            |
|     urgency_score                     |  |     recommendation_text            |
|     difficulty_score                  |  |     ai_reasoning                   |
|     weight_score                      |  |     suggested_action               |
|     effort_score                      |  |     generated_at                   |
|     workload_score                    |  +------------------------------------+
|     explanation_text                  |
|     calculated_at                     |
+---------------------------------------+
```

---

## 2. Table Schemas & Data Types

### 2.1 Table: `users`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Unique student identifier |
| `name` | VARCHAR(100) | NOT NULL | Full name of the student |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | Student university or personal email |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| `department` | VARCHAR(100) | NOT NULL | E.g. Computer Science & Engineering |
| `semester` | INT UNSIGNED | NOT NULL | 1 through 8 (or 10) |
| `college` | VARCHAR(150) | NOT NULL | Institution name |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Registration timestamp |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Profile edit timestamp |

### 2.2 Table: `subjects`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Subject ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Owner user |
| `subject_name` | VARCHAR(120) | NOT NULL | Name (e.g. Distributed Systems) |
| `subject_code` | VARCHAR(20) | NOT NULL | Code (e.g. CS602) |
| `credits` | INT UNSIGNED | NOT NULL DEFAULT 3 | Course credits (1-10) |
| `color_hex` | VARCHAR(7) | NOT NULL DEFAULT '#2563EB' | Hex color for UI badges |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

### 2.3 Table: `tasks`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Task ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Owner user |
| `subject_id` | BIGINT UNSIGNED | NOT NULL, FK -> subjects(id) ON DELETE CASCADE | Associated subject |
| `title` | VARCHAR(200) | NOT NULL | Task title |
| `description` | TEXT | NULL | Detailed notes, requirements |
| `task_type` | ENUM('ASSIGNMENT','PROJECT','EXAM','LAB','RECORD','PRESENTATION','OTHER') | NOT NULL | Academic task classification |
| `deadline` | DATETIME | NOT NULL | Due date and time |
| `difficulty` | TINYINT UNSIGNED | NOT NULL CHECK (difficulty BETWEEN 1 AND 5) | 1=Very Easy, 5=Very Hard |
| `academic_weight` | DECIMAL(5,2) | NOT NULL CHECK (academic_weight BETWEEN 0 AND 100) | % grade weight |
| `estimated_hours` | DECIMAL(4,1) | NOT NULL DEFAULT 1.0 | Estimated time to complete |
| `status` | ENUM('PENDING','IN_PROGRESS','COMPLETED','OVERDUE') | NOT NULL DEFAULT 'PENDING' | Status lifecycle |
| `completed_at` | DATETIME | NULL | Timestamp when completed |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last modification |

### 2.4 Table: `task_priorities`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Priority calculation ID |
| `task_id` | BIGINT UNSIGNED | NOT NULL, UNIQUE, FK -> tasks(id) ON DELETE CASCADE | 1:1 linked task |
| `priority_score` | DECIMAL(5,2) | NOT NULL | Normalized score (0.00 to 100.00) |
| `priority_level` | ENUM('LOW','MEDIUM','HIGH','CRITICAL') | NOT NULL | Tier level |
| `urgency_score` | DECIMAL(5,2) | NOT NULL | Computed deadline urgency |
| `difficulty_score`| DECIMAL(5,2) | NOT NULL | Normalized difficulty subscore |
| `weight_score` | DECIMAL(5,2) | NOT NULL | Normalized grade weight subscore |
| `effort_score` | DECIMAL(5,2) | NOT NULL | Normalized estimated effort subscore |
| `workload_score` | DECIMAL(5,2) | NOT NULL | Total pending load pressure subscore |
| `explanation_text`| TEXT | NOT NULL | Clear explanation for why this score was reached |
| `calculated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Last calculation timestamp |

### 2.5 Table: `study_sessions`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Session ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Student ID |
| `task_id` | BIGINT UNSIGNED | NULL, FK -> tasks(id) ON DELETE SET NULL | Optional targeted task |
| `start_time` | DATETIME | NOT NULL | Session started |
| `end_time` | DATETIME | NULL | Session ended (null if active) |
| `duration_minutes`| INT UNSIGNED | NOT NULL DEFAULT 0 | Total tracked study minutes |
| `is_completed` | BOOLEAN | NOT NULL DEFAULT FALSE | True if student stopped gracefully |
| `notes` | VARCHAR(255) | NULL | Reflection notes |

### 2.6 Table: `notifications`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Notification ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Target user |
| `task_id` | BIGINT UNSIGNED | NULL, FK -> tasks(id) ON DELETE CASCADE | Associated task if any |
| `title` | VARCHAR(120) | NOT NULL | Notification title |
| `message` | TEXT | NOT NULL | Explanatory reminder text |
| `notification_type` | ENUM('DEADLINE_48H','DEADLINE_24H','DEADLINE_6H','OVERDUE','HIGH_PRIORITY','DAILY_SUMMARY') | NOT NULL | Trigger category |
| `is_read` | BOOLEAN | NOT NULL DEFAULT FALSE | Read receipt |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Triggered at |

### 2.7 Table: `ai_recommendations`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Recommendation ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Recipient student |
| `task_id` | BIGINT UNSIGNED | NULL, FK -> tasks(id) ON DELETE CASCADE | Target task |
| `recommendation_type` | VARCHAR(50) | NOT NULL | E.g. 'WHAT_SHOULD_I_DO_NOW', 'STUDY_PLAN' |
| `recommendation_text` | TEXT | NOT NULL | Main recommendation advice |
| `ai_reasoning` | TEXT | NOT NULL | Contextual reason generated |
| `suggested_action` | VARCHAR(255) | NOT NULL | Actionable next step |
| `generated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | Generated timestamp |

### 2.8 Table: `productivity_stats`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT UNSIGNED AUTO_INCREMENT | PRIMARY KEY | Stat ID |
| `user_id` | BIGINT UNSIGNED | NOT NULL, FK -> users(id) ON DELETE CASCADE | Student ID |
| `stat_date` | DATE | NOT NULL | Aggregate date |
| `tasks_completed` | INT UNSIGNED | NOT NULL DEFAULT 0 | Completed count on that day |
| `tasks_overdue` | INT UNSIGNED | NOT NULL DEFAULT 0 | Overdue count on that day |
| `study_minutes` | INT UNSIGNED | NOT NULL DEFAULT 0 | Total focused minutes |
| `productivity_score`| DECIMAL(5,2)| NOT NULL DEFAULT 0.0 | Daily completion index |

---

## 3. Database Indexes for High Performance Queries

```sql
CREATE INDEX idx_tasks_user_status_deadline ON tasks (user_id, status, deadline);
CREATE INDEX idx_tasks_subject ON tasks (subject_id);
CREATE INDEX idx_priorities_task_score ON task_priorities (task_id, priority_score);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, is_read, created_at);
CREATE INDEX idx_sessions_user_start ON study_sessions (user_id, start_time);
CREATE INDEX idx_stats_user_date ON productivity_stats (user_id, stat_date);
```
