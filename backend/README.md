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

