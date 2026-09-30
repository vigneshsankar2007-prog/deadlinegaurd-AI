# DeadlineGuard AI — Backend (Spring Boot 3.3.4 & Java 21)

This directory contains the Spring Boot REST API backend for **DeadlineGuard AI: Intelligent Student Task & Deadline Management System**.

## Quick Start

### Prerequisites
- Java 21 (JDK 21+)
- Maven 3.9+
- MySQL 8.0+ (for `dev` and `prod` profiles)

### Build & Test
```bash
# Package JAR file
mvn clean package

# Run unit tests
mvn test

# Run application locally (connects to MySQL at localhost:3306)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Health & Public Endpoints
- `GET http://localhost:8080/api/v1/health` (Public)
- `GET http://localhost:8080/api/v1/status` (Public)
- `GET http://localhost:8080/actuator/health` (Public)

---

## Authentication & Security (Stage 4)

### Environment Variables
| Variable | Description | Default (Dev) | Production Requirement |
| :--- | :--- | :--- | :--- |
| `DB_HOST` | MySQL hostname | `localhost` | Required |
| `DB_PORT` | MySQL port | `3306` | Default 3306 |
| `DB_NAME` | MySQL database | `deadlineguard_db` | Required |
| `DB_USER` | Database username | `root` | Required |
| `DB_PASSWORD` | Database password | `rootpassword` | Required |
| `JWT_SECRET` | 256-bit cryptographically secure key | **Required via env** (`openssl rand -hex 32`) | **STRICTLY REQUIRED** |
| `JWT_EXPIRATION_MS` | Access token lifespan in ms | `86400000` (24h) | Optional |

#### Generating a Secure 256-bit JWT Secret:
```bash
openssl rand -hex 32
```

---

### Authentication Endpoints

#### 1. Student Registration
`POST /api/v1/auth/register` (Public)
```json
{
  "name": "Alex Johnson",
  "email": "alex.johnson@mit.edu",
  "password": "SecurePassword123!",
  "department": "Computer Science",
  "semester": 6,
  "college": "MIT"
}
```
**Response (201 Created):**
```json
{
  "success": true,
  "message": "Student registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "Alex Johnson",
      "email": "alex.johnson@mit.edu",
      "department": "Computer Science",
      "semester": 6,
      "college": "MIT",
      "createdAt": "2026-09-26T10:45:00"
    }
  },
  "timestamp": "2026-09-26T10:45:00"
}
```

#### 2. Student Login
`POST /api/v1/auth/login` (Public)
```json
{
  "email": "alex.johnson@mit.edu",
  "password": "SecurePassword123!"
}
```
**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "Alex Johnson",
      "email": "alex.johnson@mit.edu",
      "department": "Computer Science",
      "semester": 6,
      "college": "MIT",
      "createdAt": "2026-09-26T10:45:00"
    }
  },
  "timestamp": "2026-09-26T10:45:00"
}
```

#### 3. Current User Profile
`GET /api/v1/auth/me` (**Protected** — requires `Authorization: Bearer <token>`)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "name": "Alex Johnson",
    "email": "alex.johnson@mit.edu",
    "department": "Computer Science",
    "semester": 6,
    "college": "MIT",
    "createdAt": "2026-09-26T10:45:00"
  },
  "timestamp": "2026-09-26T10:45:00"
}
```

---

### Security Hardening Notes
1. **BCrypt Hashing**: Passwords are encrypted with salt using Spring Security's `BCryptPasswordEncoder`. Plaintext passwords and `password_hash` are never exposed in JSON responses.
2. **Account Enumeration Prevention**: Invalid email or invalid password attempts both return a generic `400 Bad Request` or `401 Unauthorized` with the uniform message `"Invalid email or password"`.
3. **Stateless Sessions**: The REST API does not store session cookies or server-side session state (`SessionCreationPolicy.STATELESS`).
4. **JWT Claims Discipline**: The JWT payload contains only minimal standard claims (`sub` = email, `iat` = issued at, `exp` = expiration). No password hashes or academic records are embedded into tokens.

---

## Subject & Task Management (Stage 5)

All endpoints require `Authorization: Bearer <token>` and enforce strict authenticated student ownership.

### Subject Endpoints (`/api/v1/subjects`)
- `POST /api/v1/subjects`: Create a new course subject (validates unique course code & name per student).
- `GET /api/v1/subjects`: List all subjects enrolled by the authenticated student.
- `GET /api/v1/subjects/{id}`: Retrieve a single subject owned by the authenticated student (returns 404 if not owned).
- `PUT /api/v1/subjects/{id}`: Update course details (credits 1–10, hex color, course code/name).
- `DELETE /api/v1/subjects/{id}`: Delete a subject. Unlinks related tasks (`subject_id = null`), preserving student deliverables.

### Task Endpoints (`/api/v1/tasks`)
- `POST /api/v1/tasks`: Create a new task deliverable. If `subjectId` is provided, verifies student owns the subject.
- `GET /api/v1/tasks`: List all tasks owned by the authenticated student (supports `?status=` and `?subjectId=` query filters, ordered by deadline ascending).
- `GET /api/v1/tasks/{id}`: Retrieve a single task owned by the authenticated student (returns 404 if not owned).
- `PUT /api/v1/tasks/{id}`: Update task title, description, deadline, difficulty, weight, estimated hours, and status.
- `DELETE /api/v1/tasks/{id}`: Delete a task owned by the authenticated student.

---

## Deterministic Priority Engine (Stage 6)

Explainable 5-factor mathematical priority calculation engine with zero external AI/LLM dependencies.

### Formula:
$$\text{PriorityScore} = 0.40 \cdot U + 0.20 \cdot D + 0.20 \cdot W + 0.10 \cdot E + 0.10 \cdot L$$
- $U$ (**Urgency**): Overdue = 100; $\le 168\text{h} \implies 100 \cdot (1 - H/168)$; $> 168\text{h} \implies \max(0, 20 \cdot (1 - (H - 168)/504))$
- $D$ (**Difficulty**): Rating $1 \dots 5 \implies ((D - 1) / 4) \cdot 100$
- $W$ (**Academic Weight**): $\min(100, \text{weight} \cdot 2.50)$
- $E$ (**Estimated Effort**): $\min(100, (\text{hours} / 10.0) \cdot 100)$
- $L$ (**Concurrent Workload**): Active student tasks in 72h window ($\pm 36\text{h}$) $\implies \min(100, \text{count} \cdot 20.00)$

### Priority Tiers:
- **CRITICAL**: 76.00 – 100.00
- **HIGH**: 51.00 – 75.99
- **MEDIUM**: 31.00 – 50.99
- **LOW**: 0.00 – 30.99

### Priority Endpoints
All require `Authorization: Bearer <token>`:
- `GET /api/v1/tasks/{taskId}/priority`: Retrieve current explainable priority breakdown for an owned task (returns 404 for unowned tasks).
- `POST /api/v1/tasks/{taskId}/priority/recalculate`: Force recalculation and persistence of priority for an owned task.
- `GET /api/v1/priorities`: Retrieve all task priorities owned by the authenticated student (optional `?sortBy=score` or `?sortBy=deadline`).

---

## Google Gemini AI Integration (Stage 7)

Contextual student academic recommendation engine using the official Google Gen AI Java SDK (`com.google.genai:google-genai:1.73.0`).

### Architecture & Grounding Contract:
- **Authoritative Determinism**: Stage 6 Priority Engine remains the single source of truth for priority scores. Gemini NEVER recalculates core scores.
- **Contextual Reasoning**: Gemini evaluates active student tasks along with their deterministic priority scores/tiers to produce actionable recommendations.
- **Zero Hallucination / Cross-Tenant Defense**: Gemini cannot inject non-existent or foreign task IDs. Responses referencing invalid task IDs fail semantic validation and safely trigger the fallback.
- **Fail-Safe Fallback**: If `GOOGLE_API_KEY` is missing, network fails, or timeout occurs, the system automatically falls back to recommending the highest-priority incomplete task with zero downtime.

### AI Endpoints
All require `Authorization: Bearer <token>`:
- `POST /api/v1/ai/what-should-i-do-now`: Generates contextual recommendation analyzing student deliverables and priorities.
- `GET /api/v1/ai/recommendations`: Lists all historical AI recommendations generated for the authenticated student.
- `GET /api/v1/ai/recommendations/{id}`: Retrieves a single recommendation by ID (only if owned by the student; returns 404 otherwise).

---

## AI Study Planner & Focus Timer (Stage 8)

Time-blocked AI study scheduling and real-time focus session tracking.

### 1. AI Study Planner (`StudyPlanService`)
Accepts available study hours, start time, and break duration to produce a non-overlapping, priority-aligned study schedule.
- Grounded in active deliverables and authoritative Stage 6 priority scores.
- Reuses Stage 7 Gemini integration with structured JSON schema (`GeminiStudyPlanSchema`).
- Deterministic fallback automatically allocates sequential blocks if AI is unavailable or produces invalid/overlapping schedules.
- Persists plan summaries into `ai_recommendations` (`recommendation_type = "STUDY_PLAN"`).

**Endpoints (`Authorization: Bearer <token>` required):**
- `POST /api/v1/ai/generate-study-plan`: Generate structured study plan.

### 2. Focus Timer & Study Session Tracking (`StudySessionService`)
Manages task-linked and general study sessions.
- **Single Active Session Rule**: Prevents concurrent active sessions per student (returns HTTP 409 Conflict).
- **Server-Authoritative Duration**: Calculates active elapsed time server-side; duration is never trusted from the client and never negative.
- **Pause & Resume**: Accurately deducts paused time without modifying the frozen database schema.
- **Ownership**: Rejects attempts to link to or stop another student's session (HTTP 404).

**Endpoints (`Authorization: Bearer <token>` required):**
- `POST /api/v1/study-sessions/start`: Start a session (`taskId` optional, `notes` optional).
- `POST /api/v1/study-sessions/{id}/stop`: Stop and complete an active session.
- `POST /api/v1/study-sessions/{id}/pause`: Pause an active session.
- `POST /api/v1/study-sessions/{id}/resume`: Resume a paused session.
- `GET /api/v1/study-sessions/active`: Retrieve current active session.
- `GET /api/v1/study-sessions`: List historical sessions (`?taskId=`, `?completed=`).
- `GET /api/v1/study-sessions/{id}`: Retrieve single session by ID.

---

## Smart Deadline Reminder & Notification System (Stage 9)

Automated deadline countdown reminders, overdue alerts, high-priority warnings, and daily academic digests.

### 1. Notification Types & Triggers
- `DEADLINE_48H`: Task deadline is within 48 hours.
- `DEADLINE_24H`: Task deadline is within 24 hours.
- `DEADLINE_6H`: Urgent alert for tasks due within 6 hours.
- `OVERDUE`: Triggered when an incomplete task has passed its deadline.
- `HIGH_PRIORITY`: Triggered when an active task meets the Stage 6 `CRITICAL` priority tier (`priorityScore >= 76.00`).
- `DAILY_SUMMARY`: Consolidated morning digest summarizing active tasks, overdue counts, and critical deliverables (at most once per day per student).

### 2. Duplicate Prevention & Time Windows
- Reminders run on non-exact lookahead windows (`hoursRemaining <= threshold`).
- Each task receives at most ONE notification per event type (`existsByUserIdAndTaskIdAndNotificationType`).
- Daily summaries are generated at most once per calendar day per student (`existsByUserIdAndNotificationTypeAndCreatedAtBetween`).

### 3. Notification API (`Authorization: Bearer <token>` required):
- `GET /api/v1/notifications`: List all notifications for authenticated student (`?unreadOnly=true` optional).
- `GET /api/v1/notifications/unread`: Shortcut to list unread notifications.
- `GET /api/v1/notifications/{id}`: Retrieve single notification (returns 404 for unowned notifications).
- `PATCH /api/v1/notifications/{id}/read`: Mark notification as read.
- `PATCH /api/v1/notifications/read-all`: Mark all notifications as read for the authenticated student.





