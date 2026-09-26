# DeadlineGuard AI
> **"Intelligent Student Task & Deadline Management System"**  
> A full-stack, AI-powered academic workload and deadline management platform engineered for undergraduate and postgraduate students.

---

## 🌟 Executive Summary

DeadlineGuard AI prevents student burnout, missed submissions, and cramming by replacing simple static to-do lists with an **explainable 5-factor mathematical priority engine** coupled with **Google Gemini AI**. The system analyzes deadline urgency, difficulty, academic weight, estimated effort, and current semester workload to answer the single most critical question every student faces: **"What should I do right now?"**

---

## 🏗️ Architecture & Technology Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Frontend** | **Flutter 3.x / Dart** | Material 3 mobile & responsive web app, blue/white student theme, dark mode, smooth animations. |
| **Backend** | **Java 21 / Spring Boot 3.3.x** | RESTful microservice API, Spring Security 6, JJWT, Spring Data JPA, Bean Validation, Lombok. |
| **Database** | **MySQL 8.0+** | Relational database with foreign key constraints, indexing, and transactional ACID guarantees. |
| **AI Intelligence**| **Google Gemini API** | Contextual study schedule synthesis, deep explanation of academic risk, and workload planning. |
| **Scoring Engine** | **PriorityCalculationService** | Deterministic weighted scoring ($0.40U + 0.20D + 0.20W + 0.10E + 0.10L$). |

---

## 📁 Complete Project Structure

```
deadlineguard-ai/
├── README.md
├── docs/
│   ├── architecture.md           # High-level architecture, layer interactions, sequence flows
│   ├── database.md               # Normalized relational schema (8 tables), data types, indexes
│   ├── api.md                    # REST API endpoints, request/response DTO schemas
│   └── ai-engine.md              # Mathematical priority scoring model & Gemini integration specs
│
├── database/
│   ├── schema.sql                # Complete DDL for MySQL (users, subjects, tasks, priorities, etc.)
│   └── seed_data.sql             # Real student test datasets for verification
│
├── backend/                      # Spring Boot 3.3 Application
│   ├── pom.xml                   # Maven dependencies (Web, Security, JPA, MySQL, JJWT, Lombok)
│   └── src/main/java/com/deadlineguard/
│       ├── controller/           # AuthController, TaskController, AIController, etc.
│       ├── service/              # PriorityCalculationService, AIService, TaskService, etc.
│       ├── entity/               # User, Subject, Task, TaskPriority, StudySession, etc.
│       ├── dto/                  # DTO request & response classes with validation
│       ├── repository/           # Spring Data JPA repositories with custom JPQL queries
│       ├── security/             # JwtAuthenticationFilter, SecurityConfig, UserDetails
│       └── exception/            # GlobalExceptionHandler with standard error envelope
│
└── frontend/                     # Flutter 3.x Cross-Platform Application
    ├── pubspec.yaml              # Dependencies (dio, provider, fl_chart, google_fonts)
    └── lib/
        ├── core/                 # Colors, themes (Material 3), network interceptors
        ├── models/               # Task, Priority, Subject, User, StudyPlan models
        ├── providers/            # State management for auth, tasks, study sessions
        ├── services/             # HTTP API client services with JWT handling
        └── ui/                   # 15 screens (Dashboard, Tasks, AI, Focus Mode, Analytics, etc.)
```

---

## 🎯 13-Stage Development Roadmap

- [x] **STAGE 1: Complete Project Architecture & Scaffolding** *(CURRENT STAGE)*
  - Full system architecture, technology stack selection, ERD specifications, REST API contracts, mathematical priority algorithms, and folder structure.
- [ ] **STAGE 2: MySQL Database Schema & Seed Data**
- [ ] **STAGE 3: Spring Boot Project Scaffolding & Configuration**
- [ ] **STAGE 4: User Authentication & Security (JWT & BCrypt)**
- [ ] **STAGE 5: Subject & Task Management CRUD APIs**
- [ ] **STAGE 6: AI Priority Engine (PriorityCalculationService)**
- [ ] **STAGE 7: Google Gemini AI Integration (Grounded Academic Reasoning)**
- [ ] **STAGE 8: AI Study Planner & Focus Timer**
- [ ] **STAGE 9: Smart Deadline Reminder System**
- [ ] **STAGE 10: Productivity Analytics & Charts**
- [ ] **STAGE 11: Flutter Frontend Architecture & Screens**
- [ ] **STAGE 12: End-to-End API Integration & State Management**
- [ ] **STAGE 13: Full System Testing & Verification**
