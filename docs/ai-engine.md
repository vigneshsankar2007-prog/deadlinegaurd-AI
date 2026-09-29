# DeadlineGuard AI — AI Priority & Reasoning Engine Specification

## 1. Explainable Priority Scoring Algorithm

The priority score is computed by `PriorityCalculationService` using a deterministic mathematical model that evaluates 5 distinct dimensions of academic pressure.

### The Mathematical Formula:
$$\text{Priority Score} = 0.40 \cdot U + 0.20 \cdot D + 0.20 \cdot W + 0.10 \cdot E + 0.10 \cdot L$$

Where:
- $U$: Urgency Score (0.00 to 100.00)
- $D$: Difficulty Score (0.00 to 100.00)
- $W$: Academic Weight Score (0.00 to 100.00)
- $E$: Estimated Effort Score (0.00 to 100.00)
- $L$: Workload Score (0.00 to 100.00)

Composite score is rounded to 2 decimal places and guaranteed in the range $[0.00, 100.00]$.

---

## 2. Explicit Subscore Mathematical Definitions & Java Implementations

### A. Urgency Score ($U \in [0.00, 100.00]$)
Measures temporal proximity to the submission deadline.
Let $H = \text{hours remaining until deadline} = \frac{\text{deadline} - \text{now}}{3600 \text{ seconds}}$.
- If $H \le 0$ (Task is overdue):
  $$U = 100.00$$
- If $0 < H \le 168.0$ (Within 7 calendar days):
  $$U = 100.00 \times \left(1.0 - \frac{H}{168.0}\right)$$
- If $H > 168.0$ (More than 7 days away):
  $$U = \max\left(0.00, 20.00 \times \left(1.0 - \frac{H - 168.0}{504.0}\right)\right)$$
  *(Scales down gently across weeks 2 to 4; beyond 4 weeks, $U = 0.00$)*

**Java Implementation:**
```java
public double calculateUrgencyScore(LocalDateTime deadline, LocalDateTime now) {
    if (deadline.isBefore(now)) return 100.00;
    double hours = Duration.between(now, deadline).toMinutes() / 60.0;
    if (hours <= 0.0) return 100.00;
    if (hours <= 168.0) {
        return Math.round((100.00 * (1.0 - (hours / 168.0))) * 100.0) / 100.0;
    }
    double baseline = Math.max(0.00, 20.00 * (1.0 - ((hours - 168.0) / 504.0)));
    return Math.round(baseline * 100.0) / 100.0;
}
```
**Examples:**
- Overdue by 1 hour $\implies U = 100.00$
- Due in 24 hours $\implies U = 100 \times (1 - 24/168) = 85.71$
- Due in 72 hours $\implies U = 100 \times (1 - 72/168) = 57.14$
- Due in 7 days (168h) $\implies U = 0.00$

---

### B. Difficulty Score ($D \in [0.00, 100.00]$)
Linear mapping of the discrete 1–5 academic rating:
$$D = \frac{\text{difficulty} - 1}{4} \times 100.00$$

**Java Implementation:**
```java
public double calculateDifficultyScore(int difficulty) {
    int clamped = Math.max(1, Math.min(5, difficulty));
    return ((clamped - 1) / 4.0) * 100.00;
}
```
**Mapping:**
- 1 (Very Easy) $\to D = 0.00$
- 2 (Easy) $\to D = 25.00$
- 3 (Moderate) $\to D = 50.00$
- 4 (Difficult) $\to D = 75.00$
- 5 (Very Difficult) $\to D = 100.00$

---

### C. Academic Weight Score ($W \in [0.00, 100.00]$)
Scales the percentage contribution of this deliverable to the overall course grade ($\text{weight} \in [0, 100]$). A major assignment or exam carrying 40% or more reaches the maximum score:
$$W = \min(100.00, \text{academic\_weight} \times 2.50)$$

**Java Implementation:**
```java
public double calculateWeightScore(double academicWeight) {
    double clamped = Math.max(0.00, Math.min(100.00, academicWeight));
    return Math.min(100.00, clamped * 2.50);
}
```
**Examples:**
- $0\%$ grade weight (extracurricular / non-credit) $\implies W = 0.00$
- $10\%$ grade weight $\implies W = 25.00$
- $20\%$ grade weight $\implies W = 50.00$
- $30\%$ grade weight $\implies W = 75.00$
- $40\%$ or greater $\implies W = 100.00$

---

### D. Estimated Effort Score ($E \in [0.00, 100.00]$)
Normalizes the estimated hours required to complete the task relative to a standard intensive workload ceiling of 10.0 hours:
$$E = \min\left(100.00, \frac{\text{estimated\_hours}}{10.0} \times 100.00\right)$$

**Java Implementation:**
```java
public double calculateEffortScore(double estimatedHours) {
    if (estimatedHours <= 0.0) return 10.00; // minimum baseline
    return Math.min(100.00, (estimatedHours / 10.0) * 100.00);
}
```
**Examples:**
- 1.5 hours $\implies E = 15.00$
- 4.0 hours $\implies E = 40.00$
- 7.0 hours $\implies E = 70.00$
- $\ge 10.0$ hours $\implies E = 100.00$

---

### E. Workload Score ($L \in [0.00, 100.00]$)
Quantifies competition from other academic deadlines within a 72-hour window centered around the task's deadline ($\pm 36\text{h}$ or $[\text{deadline} - 24\text{h}, \text{deadline} + 48\text{h}]$).
Let $C = \text{number of other active, incomplete student tasks due in this window}$.
$$L = \min(100.00, C \times 20.00)$$

**Java Implementation:**
```java
public double calculateWorkloadScore(long concurrentTaskCount) {
    return Math.min(100.00, concurrentTaskCount * 20.00);
}
```
**Examples:**
- 0 other concurrent deadlines $\implies L = 0.00$
- 1 concurrent deadline $\implies L = 20.00$
- 2 concurrent deadlines $\implies L = 40.00$
- 3 concurrent deadlines $\implies L = 60.00$
- 4 concurrent deadlines $\implies L = 80.00$
- 5 or more concurrent deadlines $\implies L = 100.00$

---

## 3. Priority Classification Tiers

| Score Range | Priority Level | UI Color Code | Operational Meaning |
| :---: | :---: | :---: | :--- |
| **76.00 – 100.00** | **CRITICAL** | Red (`#EF4444`) | Immediate intervention required; looming deadline or heavy semester impact. |
| **51.00 –  75.99** | **HIGH** | Orange (`#F97316`) | Significant academic weight or upcoming deadline within 48h. |
| **31.00 –  50.99** | **MEDIUM** | Yellow (`#EAB308`) | Active coursework progressing on normal schedule. |
| **0.00 –  30.99** | **LOW** | Green (`#10B981`) | Distant deadline, lightweight assignment, or low complexity. |

---

## 4. Gemini AI Integration & Grounding

The AI engine uses Google Gemini via structured prompt engineering.
Crucially:
- **No generic motivational platitudes**
- Grounded solely on the student's concrete tasks, deadlines, credits, and hours
- Guaranteed JSON schema output

### System Prompt Directive:
```
You are the AI engine of DeadlineGuard, an academic optimization system for university students.
Analyze the provided student tasks with their deadlines, priority scores, estimated hours, and course weights.
Deliver precise, non-generic, actionable advice without fluff.
Always output pure JSON adhering to the specified schema.
```

### JSON Schema for "What Should I Do Now":
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "recommendedTaskId": { "type": "integer" },
    "headline": { "type": "string" },
    "reason": { "type": "string" },
    "estimatedFocusTime": { "type": "integer" },
    "suggestedAction": { "type": "string" }
  },
  "required": ["recommendedTaskId", "headline", "reason", "estimatedFocusTime", "suggestedAction"]
}
```

### JSON Schema for "Generate Today's Study Plan":
```json
{
  "type": "object",
  "properties": {
    "totalStudyHours": { "type": "number" },
    "totalBreakHours": { "type": "number" },
    "schedule": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "timeSlot": { "type": "string" },
          "taskId": { "type": ["integer", "null"] },
          "taskTitle": { "type": "string" },
          "isBreak": { "type": "boolean" },
          "focusGoal": { "type": "string" }
        },
        "required": ["timeSlot", "taskTitle", "isBreak", "focusGoal"]
      }
    }
  },
  "required": ["totalStudyHours", "totalBreakHours", "schedule"]
}
```

---

## 5. Local Priority Fallback (Zero Downtime)

If the Gemini API key is unset or external network issues occur:
1. `PriorityCalculationService` still computes the mathematical priority score with 100% precision.
2. The deterministic fallback algorithm selects the task with $\max(\text{priority\_score})$ and synthesizes a structured explanation directly from the score weights.
3. The application never fails or crashes due to external AI unavailability.
