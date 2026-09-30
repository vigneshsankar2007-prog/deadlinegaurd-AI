package com.deadlineguard.service;

import com.deadlineguard.dto.ai.TaskAiContextDto;
import com.deadlineguard.dto.priority.PriorityResponse;
import com.deadlineguard.dto.study.GeminiStudyPlanSchema;
import com.deadlineguard.dto.study.GenerateStudyPlanRequest;
import com.deadlineguard.dto.study.StudyPlanResponse;
import com.deadlineguard.dto.study.StudySessionBlockDto;
import com.deadlineguard.entity.AIRecommendation;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.exception.BadRequestException;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.AIRecommendationRepository;
import com.deadlineguard.repository.TaskPriorityRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import com.deadlineguard.service.ai.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating AI-driven and deterministic fallback Study Planning.
 *
 * ARCHITECTURAL CONTRACT:
 * - Stage 6 Priority Calculation Service remains authoritative.
 * - Gemini reasons about time-allocation across real incomplete deliverables.
 * - Zero hallucination defense: Any schedule with invalid/foreign task IDs or overlapping blocks is rejected.
 * - Deterministic fallback guarantees seamless plan generation even when AI is unavailable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanService {

    public static final String REC_TYPE_STUDY_PLAN = "STUDY_PLAN";

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final PriorityCalculationService priorityCalculationService;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
            You are an academic study planning assistant for university students.
            Use ONLY the task data supplied in the prompt.
            Do NOT invent tasks, subjects, deadlines, or priority scores.
            Treat the supplied Stage 6 priority scores and tiers as strictly authoritative.
            Prioritize higher-priority tasks with imminent deadlines.
            Do NOT overlap study sessions.
            Do NOT exceed the total available study time.
            Insert the requested break duration between distinct study blocks where appropriate.

            You MUST respond in JSON strictly matching this schema:
            {
              "planDate": "YYYY-MM-DD",
              "sessions": [
                {
                  "taskId": 123,
                  "title": "Task title",
                  "subjectCode": "CS101",
                  "startTime": "YYYY-MM-DDTHH:mm:ss",
                  "endTime": "YYYY-MM-DDTHH:mm:ss",
                  "durationMinutes": 60,
                  "reason": "Brief academic rationale based on priority and deadline"
                }
              ],
              "totalStudyMinutes": 180,
              "totalBreakMinutes": 30
            }
            """;

    /**
     * Generate an AI-assisted or deterministic fallback study plan for the authenticated student.
     */
    @Transactional
    public StudyPlanResponse generateStudyPlan(Long userId, GenerateStudyPlanRequest request) {
        log.info("Generating study plan for userId: {}, availableHours: {}, startTime: {}",
                userId, request.getAvailableHours(), request.getStartTime());

        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Load active (non-completed) deliverables for the student
        List<Task> activeTasks = taskRepository.findByUserIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());

        if (activeTasks.isEmpty()) {
            log.info("Student userId: {} has zero incomplete deliverables. Returning empty plan.", userId);
            return StudyPlanResponse.builder()
                    .planDate(request.getStartTime().toLocalDate().toString())
                    .availableHours(request.getAvailableHours())
                    .startTime(request.getStartTime())
                    .breakDurationMinutes(request.getBreakDurationMinutes())
                    .sessions(Collections.emptyList())
                    .totalStudyMinutes(0)
                    .totalBreakMinutes(0)
                    .fallbackUsed(true)
                    .message("No incomplete tasks are currently available for planning.")
                    .build();
        }

        // 2. Load or compute Stage 6 authoritative priorities
        Map<Long, TaskPriority> priorityMap = new HashMap<>();
        for (Task task : activeTasks) {
            TaskPriority priority = taskPriorityRepository.findByTaskId(task.getId())
                    .orElseGet(() -> {
                        PriorityResponse resp = priorityCalculationService.calculateAndPersistPriority(task.getId(), userId);
                        return taskPriorityRepository.findByTaskId(task.getId()).orElse(null);
                    });
            if (priority != null) {
                priorityMap.put(task.getId(), priority);
            }
        }

        // Sort by Stage 6 priority score descending
        activeTasks.sort((t1, t2) -> {
            TaskPriority p1 = priorityMap.get(t1.getId());
            TaskPriority p2 = priorityMap.get(t2.getId());
            if (p1 == null || p1.getPriorityScore() == null) return 1;
            if (p2 == null || p2.getPriorityScore() == null) return -1;
            return p2.getPriorityScore().compareTo(p1.getPriorityScore());
        });

        // 3. Attempt Gemini generation with structured validation
        try {
            if (!geminiClient.isConfigured()) {
                log.warn("Gemini API key is not configured. Falling back to deterministic study planner.");
                return executeDeterministicFallback(user, activeTasks, priorityMap, request);
            }

            LocalDateTime now = LocalDateTime.now();
            List<TaskAiContextDto> contextDtos = activeTasks.stream()
                    .limit(10)
                    .map(t -> TaskAiContextDto.from(t, priorityMap.get(t.getId()), now))
                    .collect(Collectors.toList());

            Map<String, Object> promptPayload = new HashMap<>();
            promptPayload.put("availableHours", request.getAvailableHours());
            promptPayload.put("startTime", request.getStartTime().toString());
            promptPayload.put("breakDurationMinutes", request.getBreakDurationMinutes());
            promptPayload.put("tasks", contextDtos);

            String userPrompt = "Generate study plan based on student deliverables and parameters:\n"
                    + objectMapper.writeValueAsString(promptPayload);

            String rawJson = geminiClient.generateContent(SYSTEM_INSTRUCTION, userPrompt);
            StudyPlanResponse plan = parseAndValidateGeminiPlan(rawJson, activeTasks, request);

            // Persist plan recommendation into ai_recommendations table
            AIRecommendation rec = persistPlanRecommendation(user, plan, false);
            plan.setRecommendationId(rec.getId());

            log.info("Successfully generated and persisted Gemini AI Study Plan for userId: {}", userId);
            return plan;

        } catch (Exception e) {
            log.warn("Gemini study plan generation failed or invalid (reason: {}). Engaging deterministic fallback.", e.getMessage());
            return executeDeterministicFallback(user, activeTasks, priorityMap, request);
        }
    }

    private void validateRequest(GenerateStudyPlanRequest request) {
        if (request.getAvailableHours() == null || request.getAvailableHours() <= 0.0 || request.getAvailableHours() > 24.0) {
            throw new BadRequestException("availableHours must be greater than 0 and at most 24.0 hours.");
        }
        if (request.getStartTime() == null) {
            throw new BadRequestException("startTime is required.");
        }
        if (request.getBreakDurationMinutes() == null || request.getBreakDurationMinutes() < 0 || request.getBreakDurationMinutes() > 180) {
            throw new BadRequestException("breakDurationMinutes must be between 0 and 180 minutes.");
        }
    }

    /**
     * Parses and semantically validates Gemini study plan JSON.
     * Rejects overlapping blocks, foreign task IDs, or total time violations.
     */
    public StudyPlanResponse parseAndValidateGeminiPlan(
            String rawJson,
            List<Task> allowedTasks,
            GenerateStudyPlanRequest request
    ) throws Exception {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini returned empty study plan");
        }

        String json = rawJson.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        } else if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();

        GeminiStudyPlanSchema schema = objectMapper.readValue(json, GeminiStudyPlanSchema.class);

        if (schema.getSessions() == null || schema.getSessions().isEmpty()) {
            throw new IllegalArgumentException("Gemini returned zero study sessions");
        }

        Map<Long, Task> taskMap = allowedTasks.stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        List<StudySessionBlockDto> blocks = new ArrayList<>();
        LocalDateTime lastEnd = null;
        int totalStudyMinutes = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        int maxAllowedMinutes = (int) Math.round(request.getAvailableHours() * 60.0);

        for (GeminiStudyPlanSchema.SessionBlockSchema session : schema.getSessions()) {
            if (session.getTaskId() == null || !taskMap.containsKey(session.getTaskId())) {
                throw new IllegalArgumentException("Gemini referenced invalid or foreign taskId: " + session.getTaskId());
            }

            Task task = taskMap.get(session.getTaskId());
            LocalDateTime start = LocalDateTime.parse(session.getStartTime(), formatter);
            LocalDateTime end = LocalDateTime.parse(session.getEndTime(), formatter);

            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("Session end time must be after start time");
            }

            int duration = session.getDurationMinutes() != null && session.getDurationMinutes() > 0
                    ? session.getDurationMinutes()
                    : (int) java.time.Duration.between(start, end).toMinutes();

            // Strict overlap prevention: current start must not be before last end
            if (lastEnd != null && start.isBefore(lastEnd)) {
                throw new IllegalArgumentException("Gemini generated overlapping study session blocks");
            }

            lastEnd = end;
            totalStudyMinutes += duration;

            blocks.add(StudySessionBlockDto.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .subjectCode(task.getSubject() != null ? task.getSubject().getSubjectCode() : session.getSubjectCode())
                    .startTime(start)
                    .endTime(end)
                    .durationMinutes(duration)
                    .reason(session.getReason() != null ? session.getReason() : "Priority-aligned deliverable focus")
                    .build());
        }

        int totalBreakMinutes = schema.getTotalBreakMinutes() != null ? schema.getTotalBreakMinutes() : 0;
        if (totalStudyMinutes + totalBreakMinutes > maxAllowedMinutes + 5) { // 5-min tolerance
            throw new IllegalArgumentException("Study plan total duration exceeds requested available time: "
                    + (totalStudyMinutes + totalBreakMinutes) + "m > " + maxAllowedMinutes + "m");
        }

        return StudyPlanResponse.builder()
                .planDate(schema.getPlanDate() != null ? schema.getPlanDate() : request.getStartTime().toLocalDate().toString())
                .availableHours(request.getAvailableHours())
                .startTime(request.getStartTime())
                .breakDurationMinutes(request.getBreakDurationMinutes())
                .sessions(blocks)
                .totalStudyMinutes(totalStudyMinutes)
                .totalBreakMinutes(totalBreakMinutes)
                .fallbackUsed(false)
                .message("Study plan generated successfully by Gemini AI.")
                .build();
    }

    /**
     * Deterministic fallback algorithm:
     * Allocates sequential, non-overlapping study blocks based strictly on Stage 6 priority scores.
     */
    public StudyPlanResponse executeDeterministicFallback(
            User user,
            List<Task> sortedTasks,
            Map<Long, TaskPriority> priorityMap,
            GenerateStudyPlanRequest request
    ) {
        log.info("Executing deterministic study planner fallback for userId: {}", user.getId());

        int availableMinutes = (int) Math.round(request.getAvailableHours() * 60.0);
        int breakMinutes = request.getBreakDurationMinutes();

        LocalDateTime currentTime = request.getStartTime();
        List<StudySessionBlockDto> blocks = new ArrayList<>();
        int totalStudyMinutes = 0;
        int totalBreakMinutes = 0;

        for (int i = 0; i < sortedTasks.size() && (totalStudyMinutes + totalBreakMinutes < availableMinutes); i++) {
            Task task = sortedTasks.get(i);
            TaskPriority priority = priorityMap.get(task.getId());

            int remainingMinutes = availableMinutes - (totalStudyMinutes + totalBreakMinutes);
            if (remainingMinutes < 15) {
                break; // Not enough time for a viable study block
            }

            // Allocate 45 to 90 minutes per deliverable, bounded by task estimated hours
            int idealMinutes = 60;
            if (task.getEstimatedHours() != null) {
                int estMinutes = (int) Math.round(task.getEstimatedHours().doubleValue() * 60.0);
                idealMinutes = Math.max(30, Math.min(90, estMinutes));
            }

            int sessionDuration = Math.min(idealMinutes, remainingMinutes);
            LocalDateTime blockEnd = currentTime.plusMinutes(sessionDuration);

            String scoreStr = (priority != null && priority.getPriorityScore() != null)
                    ? priority.getPriorityScore().toString()
                    : "High";
            String tierStr = (priority != null && priority.getPriorityLevel() != null)
                    ? priority.getPriorityLevel().name()
                    : "CRITICAL";

            String reason = String.format("Deterministic fallback prioritized based on Stage 6 score: %s (%s). Due: %s.",
                    scoreStr, tierStr, task.getDeadline());

            blocks.add(StudySessionBlockDto.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .subjectCode(task.getSubject() != null ? task.getSubject().getSubjectCode() : null)
                    .startTime(currentTime)
                    .endTime(blockEnd)
                    .durationMinutes(sessionDuration)
                    .reason(reason)
                    .build());

            totalStudyMinutes += sessionDuration;
            currentTime = blockEnd;

            // Insert break if there are remaining deliverables and remaining time
            remainingMinutes = availableMinutes - (totalStudyMinutes + totalBreakMinutes);
            if (i < sortedTasks.size() - 1 && breakMinutes > 0 && remainingMinutes >= (breakMinutes + 15)) {
                currentTime = currentTime.plusMinutes(breakMinutes);
                totalBreakMinutes += breakMinutes;
            }
        }

        StudyPlanResponse plan = StudyPlanResponse.builder()
                .planDate(request.getStartTime().toLocalDate().toString())
                .availableHours(request.getAvailableHours())
                .startTime(request.getStartTime())
                .breakDurationMinutes(request.getBreakDurationMinutes())
                .sessions(blocks)
                .totalStudyMinutes(totalStudyMinutes)
                .totalBreakMinutes(totalBreakMinutes)
                .fallbackUsed(true)
                .message("Deterministic fallback plan generated based on authoritative Stage 6 priorities.")
                .build();

        AIRecommendation rec = persistPlanRecommendation(user, plan, true);
        plan.setRecommendationId(rec.getId());

        return plan;
    }

    private AIRecommendation persistPlanRecommendation(User user, StudyPlanResponse plan, boolean fallback) {
        String headline = String.format("Study Plan (%s): %d study blocks scheduled",
                plan.getPlanDate(), plan.getSessions().size());
        String reasoning = fallback
                ? "Deterministic fallback scheduled tasks in sequence according to Stage 6 composite priority scores."
                : "Gemini AI allocated focus blocks matching your requested available time and task deadlines.";
        String action = plan.getSessions().isEmpty()
                ? "Enjoy your break."
                : "Begin your first focus block: " + plan.getSessions().get(0).getTitle();

        Task singleTask = plan.getSessions().size() == 1
                ? taskRepository.findById(plan.getSessions().get(0).getTaskId()).orElse(null)
                : null;

        AIRecommendation rec = AIRecommendation.builder()
                .user(user)
                .task(singleTask)
                .recommendationType(REC_TYPE_STUDY_PLAN)
                .recommendationText(headline)
                .aiReasoning(reasoning)
                .suggestedAction(action)
                .generatedAt(LocalDateTime.now())
                .build();

        return aiRecommendationRepository.save(rec);
    }
}
