# DeadlineGuard AI — REST API Specification

All protected endpoints require an `Authorization: Bearer <JWT_TOKEN>` header.
Standard JSON format is used across all responses.

---

## 1. Standard API Response Formats

### Success Response Envelope
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-09-26T10:15:30Z"
}
```

### Error Response Envelope (RFC-7807 compatible)
```json
{
  "success": false,
  "message": "Task not found with ID: 42",
  "status": 404,
  "errors": null,
  "timestamp": "2026-09-26T10:15:30Z"
}
```

---

## 2. Authentication Endpoints

### 2.1 POST `/api/auth/register`
Creates a new student account.
* **Request Body:**
```json
{
  "name": "Alex Johnson",
  "email": "alex.j@university.edu",
  "password": "SecurePassword123!",
  "department": "Computer Science & Engineering",
  "semester": 6,
  "college": "National Institute of Technology"
}
```
* **Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "Alex Johnson",
      "email": "alex.j@university.edu",
      "department": "Computer Science & Engineering",
      "semester": 6,
      "college": "National Institute of Technology"
    }
  },
  "message": "User registered successfully"
}
```

### 2.2 POST `/api/auth/login`
Authenticates user and returns signed JWT token.
* **Request Body:**
```json
{
  "email": "alex.j@university.edu",
  "password": "SecurePassword123!"
}
```
* **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": { ... }
  },
  "message": "Login successful"
}
```

---

## 3. User Profile Endpoints

### 3.1 GET `/api/users/me`
* **Response (200 OK):** Current logged-in student's full profile.

### 3.2 PUT `/api/users/me`
* **Request Body:** `{ "name": "Alex J.", "department": "Data Science", "semester": 7, "college": "NIT" }`
* **Response (200 OK):** Updated user profile.

---

## 4. Subject Endpoints

* **POST `/api/subjects`** (201 Created):
  `{ "subjectName": "Operating Systems", "subjectCode": "CS602", "credits": 4, "colorHex": "#3B82F6" }`
* **GET `/api/subjects`** (200 OK): List of student's subjects with course credits and assigned task count.
* **PUT `/api/subjects/{id}`** (200 OK): Update subject details.
* **DELETE `/api/subjects/{id}`** (204 No Content): Deletes subject and cascading tasks.

---

## 5. Task Management Endpoints

### 5.1 POST `/api/tasks`
Creates a task and immediately triggers priority calculation.
* **Request Body:**
```json
{
  "subjectId": 2,
  "title": "Operating Systems Assignment",
  "description": "Implement virtual memory simulation with Page Replacement Algorithms (LRU, FIFO)",
  "taskType": "ASSIGNMENT",
  "deadline": "2026-09-28T23:59:00",
  "difficulty": 5,
  "academicWeight": 20.0,
  "estimatedHours": 3.5
}
```
* **Response (201 Created):** Task entity + calculated Priority breakdown.

### 5.2 GET `/api/tasks`
* Query parameters: `?status=PENDING&subjectId=2&search=memory&sortBy=priority`
* Returns paginated or filtered list of tasks.

### 5.3 GET `/api/tasks/{id}`
Returns single task along with full priority and AI explanation.

### 5.4 PUT `/api/tasks/{id}`
Updates task metadata and triggers priority recalculation.

### 5.5 PATCH `/api/tasks/{id}/complete`
Marks task as COMPLETED, records `completed_at`, and rebalances workload scores for remaining tasks.

---

## 6. Priority Engine Endpoints

### 6.1 GET `/api/tasks/prioritized`
Returns all active tasks sorted by `priority_score DESC` with all 5 component scores:
```json
{
  "success": true,
  "data": [
    {
      "taskId": 101,
      "title": "Operating Systems Assignment",
      "priorityScore": 87.4,
      "priorityLevel": "CRITICAL",
      "urgencyScore": 92.0,
      "difficultyScore": 100.0,
      "weightScore": 80.0,
      "effortScore": 70.0,
      "workloadScore": 85.0,
      "explanationText": "CRITICAL priority: Due in 28 hours, highest difficulty level (5/5), and accounts for 20% of your course grade."
    }
  ]
}
```

---

## 7. AI Endpoints (Google Gemini Powered)

### 7.1 POST `/api/ai/what-should-i-do`
Finds the single most critical task for right now with verified reasons and focus steps.
* **Response (200 OK):**
```json
{
  "recommendedTask": {
    "id": 101,
    "title": "Operating Systems Assignment",
    "subject": "CS602 - Operating Systems",
    "deadline": "2026-09-28T23:59:00",
    "priorityScore": 87.4,
    "estimatedHours": 3.5
  },
  "headline": "Focus on Operating Systems Assignment now",
  "reason": "It has your highest priority score (87.4), is due in less than 2 days, and accounts for 20% of your semester grade with high algorithmic complexity.",
  "estimatedFocusTime": 120,
  "suggestedAction": "Implement the LRU algorithm logic first before starting the benchmarking report."
}
```

### 7.2 POST `/api/ai/generate-study-plan`
* **Request Body:**
```json
{
  "availableHours": 4.0,
  "startTime": "18:00",
  "breakDurationMinutes": 15
}
```
* **Response (200 OK):**
```json
{
  "totalStudyHours": 3.5,
  "totalBreakHours": 0.5,
  "schedule": [
    {
      "timeSlot": "6:00 PM - 7:30 PM",
      "taskTitle": "Operating Systems Assignment",
      "taskType": "ASSIGNMENT",
      "isBreak": false,
      "focusGoal": "Write the FIFO & LRU page replacement algorithm simulations"
    },
    {
      "timeSlot": "7:30 PM - 7:45 PM",
      "taskTitle": "Rest & Hydration Break",
      "isBreak": true,
      "focusGoal": "Step away from screen"
    },
    {
      "timeSlot": "7:45 PM - 8:45 PM",
      "taskTitle": "Java Lab - Multithreading",
      "taskType": "LAB",
      "isBreak": false,
      "focusGoal": "Complete thread pool synchronization exercises"
    },
    {
      "timeSlot": "8:45 PM - 9:00 PM",
      "taskTitle": "Break",
      "isBreak": true,
      "focusGoal": "Quick walk"
    },
    {
      "timeSlot": "9:00 PM - 10:00 PM",
      "taskTitle": "Cyber Security Revision",
      "taskType": "EXAM",
      "isBreak": false,
      "focusGoal": "Review RSA and AES cryptography questions"
    }
  ]
}
```

---

## 8. Study Session Tracking Endpoints

* **POST `/api/study-sessions/start`**:
  `{ "taskId": 101 }` -> Returns active session ID and start timestamp.
* **POST `/api/study-sessions/{id}/stop`**:
  `{ "completed": true, "notes": "Completed FIFO implementation" }` -> Calculates duration and updates metrics.
* **GET `/api/study-sessions`**: Lists historical sessions with duration summaries.

---

## 9. Analytics & Notifications Endpoints

* **GET `/api/analytics/dashboard`**: Overall progress stats, weekly completion rate, completion ratio.
* **GET `/api/analytics/weekly`**: Daily completed vs overdue distribution for past 7 days.
* **GET `/api/analytics/subjects`**: Workload and credit-weighted effort per subject.
* **GET `/api/notifications`**: List unread and read notifications.
* **PATCH `/api/notifications/{id}/read`**: Mark specific alert as acknowledged.
