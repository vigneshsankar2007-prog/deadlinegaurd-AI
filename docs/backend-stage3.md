# DeadlineGuard AI — Stage 3: Spring Boot Scaffolding & Configuration Report

## 1. Executive Summary
Stage 3 establishes the enterprise-grade foundation for the **DeadlineGuard AI** backend using **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA / Hibernate 6.x**, and **MySQL 8.0+**.

The codebase strictly adheres to the frozen Stage 2 database architecture:
- 8 JPA Entities matching the 8 normalized MySQL tables.
- 8 Spring Data Repositories providing derived query methods and JPQL aggregations.
- Complete application configurations for `dev`, `prod`, and `test` environments.
- High-performance HikariCP connection pooling configured with connection test queries and timeouts.
- Standardized API response envelopes (`ApiResponse<T>`, `ErrorResponse`).
- Centralized exception interception (`GlobalExceptionHandler`) with Bean Validation error mapping.
- Health monitoring and metadata endpoints (`/api/v1/health` and `/api/v1/status`).
- Unit testing harness (`application-test.yml` with in-memory test database).

---

## 2. Technology Stack & Dependencies (`pom.xml`)
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.4 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`)
- **Database Driver**: `mysql-connector-j` (MySQL 8.0+)
- **Test Database**: `com.h2database:h2` (MySQL compatibility mode for zero-external-dependency unit tests)
- **Boilerplate Reduction**: Project Lombok (with Maven annotation processor path configured)
- **Validation**: Jakarta Bean Validation (`hibernate-validator`)

---

## 3. Package & Directory Structure
```
backend/
├── pom.xml                                           # Maven dependencies & build plugins
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── deadlineguard/
│   │   │           ├── DeadlineGuardApplication.java # Spring Boot entry point with logging
│   │   │           ├── common/
│   │   │           │   ├── ApiResponse.java          # Standard REST response envelope
│   │   │           │   ├── ErrorResponse.java        # Structured error payload with field errors
│   │   │           │   └── GlobalExceptionHandler.java # @RestControllerAdvice interceptor
│   │   │           ├── config/
│   │   │           │   ├── CorsConfig.java           # Cross-origin config for Flutter clients
│   │   │           │   └── JpaConfig.java            # Transaction & repository enablement
│   │   │           ├── controller/
│   │   │           │   └── HealthController.java     # /api/v1/health & /api/v1/status
│   │   │           ├── entity/
│   │   │           │   ├── User.java                 # Mapped to `users` table
│   │   │           │   ├── Subject.java              # Mapped to `subjects` table
│   │   │           │   ├── Task.java                 # Mapped to `tasks` table
│   │   │           │   ├── TaskPriority.java         # Mapped to `task_priorities` table
│   │   │           │   ├── StudySession.java         # Mapped to `study_sessions` table
│   │   │           │   ├── Notification.java         # Mapped to `notifications` table
│   │   │           │   ├── AIRecommendation.java     # Mapped to `ai_recommendations` table
│   │   │           │   ├── ProductivityStat.java     # Mapped to `productivity_stats` table
│   │   │           │   └── enums/
│   │   │           │       ├── TaskType.java         # ASSIGNMENT, PROJECT, EXAM, LAB, etc.
│   │   │           │       ├── TaskStatus.java       # PENDING, IN_PROGRESS, COMPLETED, OVERDUE
│   │   │           │       ├── PriorityLevel.java    # LOW, MEDIUM, HIGH, CRITICAL
│   │   │           │       └── NotificationType.java # DEADLINE_48H/24H/6H, OVERDUE, etc.
│   │   │           ├── exception/
│   │   │           │   ├── ResourceNotFoundException.java # HTTP 404
│   │   │           │   ├── BadRequestException.java       # HTTP 400
│   │   │           │   └── DuplicateResourceException.java# HTTP 409
│   │   │           └── repository/
│   │   │               ├── UserRepository.java
│   │   │               ├── SubjectRepository.java
│   │   │               ├── TaskRepository.java
│   │   │               ├── TaskPriorityRepository.java
│   │   │               ├── StudySessionRepository.java
│   │   │               ├── NotificationRepository.java
│   │   │               ├── AIRecommendationRepository.java
│   │   │               └── ProductivityStatRepository.java
│   │   └── resources/
│   │       ├── application.yml                       # Base application configuration
│   │       ├── application-dev.yml                   # Dev profile (local MySQL + HikariCP)
│   │       ├── application-prod.yml                  # Prod profile (managed MySQL / Cloud SQL)
│   │       └── banner.txt                            # ANSI startup banner
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── deadlineguard/
│       │           └── DeadlineGuardApplicationTests.java # Context-load verification test
│       └── resources/
│           └── application-test.yml                  # In-memory H2 MySQL mode test DB
```

---

## 4. Entity Mappings & Relational Integrity

### A. Subject Ownership & Task Independence
In accordance with Stage 2 frozen specification:
- `Task.subject` is marked `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "subject_id", nullable = true)`.
- If a course/subject is deleted, the database sets `subject_id = NULL` via `ON DELETE SET NULL`, preserving the student's tasks intact.
- Subject ownership is strictly enforced at the application service layer: `task.getUser().getId().equals(subject.getUser().getId())`.

### B. 5-Factor Priority Mathematical Integrity
`TaskPriority.java` contains helper methods to determine the categorical level based on frozen thresholds:
- `76.00 - 100.00`: `CRITICAL`
- `51.00 - 75.99`: `HIGH`
- `31.00 - 50.99`: `MEDIUM`
- `0.00 - 30.99`: `LOW`

And calculates composite priority using exact weights:
$$\text{priority\_score} = 0.40 \cdot U + 0.20 \cdot D + 0.20 \cdot W + 0.10 \cdot E + 0.10 \cdot L$$

---

## 5. Build & Execution Commands
```bash
# Compile and package application
cd backend
mvn clean package

# Run unit tests (uses in-memory H2 DB)
mvn test

# Run application locally with development profile (requires MySQL 8 running)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with custom database credentials
DB_HOST=localhost DB_PORT=3306 DB_NAME=deadlineguard_db DB_USER=root DB_PASSWORD=secret \
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
