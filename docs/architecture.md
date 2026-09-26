# DeadlineGuard AI — System Architecture & Blueprints

## 1. System Overview
**DeadlineGuard AI** is an intelligent academic task and deadline management system engineered for undergraduate and postgraduate students. It solves academic burnout and poor prioritization by combining an explainable weighted priority scoring engine with Google Gemini AI to provide dynamic workload analysis, personalized study scheduling, and intelligent recommendations.

```
+-----------------------------------------------------------------------------------+
|                           CLIENT LAYER (Flutter 3.x)                             |
|  - Material 3 Mobile & Responsive Web UI                                          |
|  - State Management: Provider / Riverpod                                          |
|  - Dio HTTP Client with JWT interceptors & secure token storage                   |
+------------------------------------------+----------------------------------------+
                                           | HTTPS / JSON (REST APIs)
                                           v
+-----------------------------------------------------------------------------------+
|                        API & SECURITY LAYER (Spring Boot 3)                       |
|  - Spring Security 6 + JJWT Filter                                                |
|  - CORS & Rate Limiting Filter                                                    |
|  - Controller Advice (Global RFC-7807 Exception Handling)                         |
+------------------------------------------+----------------------------------------+
                                           |
        +----------------------------------+----------------------------------+
        |                                                                     |
        v                                                                     v
+----------------------------------+               +----------------------------------+
|    CORE BUSINESS LOGIC LAYER     |               |         AI & REASONING           |
|  - Task & Subject Services       |               |  - PriorityCalculationService    |
|  - StudySessionService           |               |    (Deterministic 0-100 formula) |
|  - NotificationService           | <-----------> |  - AIService (Gemini 2.5 Flash   |
|  - Analytics & Workload Service  |               |    via @google/genai / REST)     |
+----------------------------------+               +----------------------------------+
        |                                                                     |
        +----------------------------------+----------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                        DATA PERSISTENCE LAYER (JPA / Hibernate)                  |
|  - Spring Data JPA Repositories                                                   |
|  - Connection Pooling (HikariCP)                                                  |
|  - Flyway / Schema Migrations                                                     |
+------------------------------------------+----------------------------------------+
                                           | SQL / TCP Port 3306
                                           v
+-----------------------------------------------------------------------------------+
|                        DATABASE LAYER (MySQL 8.0+)                               |
|  - Relational Schema with Foreign Keys, Cascades, Indexes                         |
|  - UTF8mb4 Character Set                                                          |
|  - 8 Normalized Tables: users, subjects, tasks, task_priorities,                  |
|    study_sessions, notifications, ai_recommendations, productivity_stats          |
+-----------------------------------------------------------------------------------+
```

---

## 2. Directory Structure

```
deadlineguard-ai/
├── README.md
├── docs/
│   ├── architecture.md
│   ├── database.md
│   ├── api.md
│   └── ai-engine.md
│
├── database/
│   ├── schema.sql
│   └── seed_data.sql
│
├── backend/                                   # Java Spring Boot 3.3.x Backend
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/deadlineguard/
│       │   │   ├── DeadlineGuardApplication.java
│       │   │   ├── config/                    # Security, CORS, Swagger, AppConfig
│       │   │   ├── controller/                # REST Controllers
│       │   │   ├── dto/                       # Request & Response DTOs
│       │   │   │   ├── request/
│       │   │   │   └── response/
│       │   │   ├── entity/                    # JPA Entities
│       │   │   ├── enums/                     # TaskStatus, TaskType, PriorityLevel
│       │   │   ├── exception/                 # GlobalExceptionHandler & custom errors
│       │   │   ├── repository/                # Spring Data JPA Repositories
│       │   │   ├── security/                  # JWT Filter, UserDetails, TokenProvider
│       │   │   ├── service/                   # Business Services
│       │   │   │   └── impl/
│       │   │   ├── ai/                        # Gemini Client, Prompt Templates, Parsers
│       │   │   └── util/                      # Date/Time helpers, Math normalizers
│       │   └── resources/
│       │       ├── application.yml
│       │       └── application-prod.yml
│       └── test/
│           └── java/com/deadlineguard/
│
└── frontend/                                  # Flutter 3.x Mobile & Web App
    ├── pubspec.yaml
    ├── analysis_options.yaml
    └── lib/
        ├── main.dart
        ├── core/
        │   ├── constants/                     # Colors, API endpoints, themes
        │   ├── network/                       # Dio client & Auth interceptor
        │   ├── theme/                         # Material 3 Light & Dark themes
        │   └── utils/                         # Formatters, dialogs
        ├── models/                            # Dart data models with JSON serialization
        ├── providers/                         # State management (Task, Auth, Study, AI)
        ├── services/                          # API Service layer
        └── ui/
            ├── screens/
            │   ├── splash/
            │   ├── auth/                      # Login, Register
            │   ├── dashboard/                 # Home dashboard & "What to do now"
            │   ├── tasks/                     # Task list, Add/Edit task, Details
            │   ├── subjects/                  # Subject management
            │   ├── ai/                        # AI Study Planner & Recommendations
            │   ├── focus/                     # Study Session & Pomodoro Focus Timer
            │   ├── notifications/             # Notification center
            │   ├── analytics/                 # Productivity charts & breakdown
            │   └── profile/                   # Student profile & settings
            └── widgets/                       # Reusable UI widgets
```

---

## 3. Technology Stack Justification

| Layer | Technology | Version | Justification |
| :--- | :--- | :--- | :--- |
| **Mobile/Web Frontend** | Flutter / Dart | Flutter 3.24+ / Dart 3.5+ | Single codebase for Android, iOS, and Web. Native 60-120fps Material 3 animations and custom charting support. |
| **Backend Framework** | Java / Spring Boot | Java 21 LTS / Spring Boot 3.3+ | Enterprise-grade type safety, mature security ecosystem (Spring Security 6), robust connection pooling, and multi-threaded scheduled tasks. |
| **Build & Dependency** | Maven / Gradle | Maven 3.9+ | Standardized lifecycle management for Spring Boot ecosystem. |
| **ORM / Data Access** | Spring Data JPA / Hibernate | 6.5+ | High performance object-relational mapping, lazy loading, and schema validation. |
| **Authentication** | JJWT (io.jsonwebtoken) | 0.12.5 | Stateless, cryptographically signed tokens with expiration and claims. |
| **Relational Database** | MySQL | 8.0+ / 8.4 LTS | ACID compliance, robust foreign key indexing, JSON functions, and date-time calculations. |
| **AI Reasoning Engine** | Google Gemini API | gemini-2.5-flash | High throughput, low latency, structured JSON response mode, and reasoning capability for study plans. |
