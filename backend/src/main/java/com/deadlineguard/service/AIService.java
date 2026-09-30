package com.deadlineguard.service;

import com.deadlineguard.dto.ai.AIRecommendationResponse;
import com.deadlineguard.dto.ai.GeminiRecommendationSchema;
import com.deadlineguard.dto.ai.TaskAiContextDto;
import com.deadlineguard.dto.priority.PriorityResponse;
import com.deadlineguard.entity.AIRecommendation;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing Google Gemini AI contextual recommendations for students.
 *
 * ARCHITECTURAL CONTRACT:
 * - Deterministic Priority Engine (Stage 6) remains the authoritative source for priority scores.
 * - Gemini provides contextual reasoning, recommendation phrasing, and suggested next actions.
 * - Robust fail-safe fallback guarantees zero single point of failure if Gemini API is unavailable or errors out.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    public static final String REC_TYPE_WHAT_SHOULD_I_DO_NOW = "WHAT_SHOULD_I_DO_NOW";

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskPriorityRepository taskPriorityRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final PriorityCalculationService priorityCalculationService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_INSTRUCTION = """
            You are an academic task recommendation assistant for university students.
            Use ONLY the task data supplied in the user prompt.
            Do not invent deadlines, scores, subjects, grades, study hours, or tasks.
            Do not recalculate the deterministic priority score.
            Treat the supplied priority score and priority tier as authoritative.
            Recommend a concrete next action.
            Prefer actionable and concise reasoning.
            Do not produce generic motivational content.
            Do not claim facts that are absent from the supplied data.
            Never expose private credentials.
            If the supplied data is insufficient, say so in the structured response.

            You MUST respond with a JSON object strictly matching this schema:
            {
              "recommendationType": "WHAT_SHOULD_I_DO_NOW",
              "recommendationText": "Concise headline recommendation (max 100 chars)",
              "aiReasoning": "Grounded explanation referencing specific task title, course code, deadline, and priority metrics",
              "suggestedAction": "One concrete next action for the student's next study block (max 255 chars)",
              "taskId": 123
            }
            """;

    /**
     * Generate "What should I do now?" recommendation for the authenticated student.
     * Uses Gemini AI with deterministic priority engine fallback.
     */
    @Transactional
    public AIRecommendationResponse generateWhatShouldIDoNow(Long userId) {
        log.info("Generating 'What should I do now?' recommendation for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Gather all active (non-completed) deliverables belonging exclusively to the student
        List<Task> activeTasks = getActiveTasksForUser(userId);

        if (activeTasks.isEmpty()) {
            log.info("Student userId: {} has zero incomplete deliverables. Generating completion fallback.", userId);
            return executeFallbackNoTasks(user);
        }

        // 2. Fetch or compute deterministic priority for each task, and rank by priority score descending
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

        // Sort tasks by deterministic priority score DESC (authoritative order)
        activeTasks.sort((t1, t2) -> {
            TaskPriority p1 = priorityMap.get(t1.getId());
            TaskPriority p2 = priorityMap.get(t2.getId());
            if (p1 == null || p1.getPriorityScore() == null) return 1;
            if (p2 == null || p2.getPriorityScore() == null) return -1;
            return p2.getPriorityScore().compareTo(p1.getPriorityScore());
        });

        // 3. Take top 10 relevant deliverables to keep context concise and grounded
        List<Task> topTasks = activeTasks.stream().limit(10).collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();

        List<TaskAiContextDto> contextDtos = topTasks.stream()
                .map(t -> TaskAiContextDto.from(t, priorityMap.get(t.getId()), now))
                .collect(Collectors.toList());

        // 4. Attempt Gemini generation with structured validation
        try {
            if (!geminiClient.isConfigured()) {
                log.warn("Gemini API key is not configured. Falling back to deterministic priority engine.");
                return executeDeterministicFallback(user, topTasks, priorityMap);
            }

            String userPrompt = "Student Active Tasks and Authoritative Priority Breakdown:\n"
                    + objectMapper.writeValueAsString(contextDtos);

            log.debug("Invoking Gemini for userId: {} with {} active tasks in context", userId, contextDtos.size());
            String rawJson = geminiClient.generateContent(SYSTEM_INSTRUCTION, userPrompt);

            // 5. Parse structured output
            GeminiRecommendationSchema schema = parseAndValidateGeminiResponse(rawJson, topTasks);

            // 6. Persist successful recommendation
            Task referencedTask = null;
            if (schema.getTaskId() != null) {
                referencedTask = topTasks.stream()
                        .filter(t -> t.getId().equals(schema.getTaskId()))
                        .findFirst()
                        .orElse(null);
            }

            AIRecommendation recommendation = AIRecommendation.builder()
                    .user(user)
                    .task(referencedTask)
                    .recommendationType(REC_TYPE_WHAT_SHOULD_I_DO_NOW)
                    .recommendationText(schema.getRecommendationText().trim())
                    .aiReasoning(schema.getAiReasoning().trim())
                    .suggestedAction(schema.getSuggestedAction().trim())
                    .generatedAt(LocalDateTime.now())
                    .build();

            AIRecommendation saved = aiRecommendationRepository.save(recommendation);
            log.info("Persisted Gemini AI recommendation id: {} for userId: {}", saved.getId(), userId);
            return AIRecommendationResponse.fromEntity(saved, false);

        } catch (Exception e) {
            log.warn("Gemini AI generation failed or invalid (reason: {}). Engaging deterministic fallback.", e.getMessage());
            return executeDeterministicFallback(user, topTasks, priorityMap);
        }
    }

    /**
     * Retrieve all previous recommendations generated for the authenticated student.
     */
    @Transactional(readOnly = true)
    public List<AIRecommendationResponse> getRecommendations(Long userId) {
        return aiRecommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId)
                .stream()
                .map(AIRecommendationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific recommendation only if owned by the authenticated student.
     */
    @Transactional(readOnly = true)
    public AIRecommendationResponse getRecommendationById(Long id, Long userId) {
        AIRecommendation recommendation = aiRecommendationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AIRecommendation", "id", id));
        return AIRecommendationResponse.fromEntity(recommendation);
    }

    // =========================================================================
    // INTERNAL VALIDATION & FAIL-SAFE DETERMINISTIC FALLBACK METHODS
    // =========================================================================

    private List<Task> getActiveTasksForUser(Long userId) {
        return taskRepository.findByUserIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    /**
     * Parses and semantically validates structured response from Gemini.
     * Rejects malformed JSON, missing fields, or hallucinated task IDs.
     */
    public GeminiRecommendationSchema parseAndValidateGeminiResponse(String rawJson, List<Task> allowedTasks) throws Exception {
        if (rawJson == null || rawJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini returned empty response");
        }

        // Clean any accidental markdown code block ticks if present
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

        GeminiRecommendationSchema schema = objectMapper.readValue(json, GeminiRecommendationSchema.class);

        if (schema.getRecommendationText() == null || schema.getRecommendationText().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: recommendationText");
        }
        if (schema.getAiReasoning() == null || schema.getAiReasoning().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: aiReasoning");
        }
        if (schema.getSuggestedAction() == null || schema.getSuggestedAction().trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required field: suggestedAction");
        }

        // Validate recommendation type
        if (!REC_TYPE_WHAT_SHOULD_I_DO_NOW.equalsIgnoreCase(schema.getRecommendationType())) {
            throw new IllegalArgumentException("Invalid recommendationType: " + schema.getRecommendationType());
        }

        // Validate task ID ownership: if present, MUST exist in allowedTasks
        if (schema.getTaskId() != null) {
            boolean matches = allowedTasks.stream().anyMatch(t -> t.getId().equals(schema.getTaskId()));
            if (!matches) {
                throw new IllegalArgumentException("Gemini referenced invalid or cross-tenant taskId: " + schema.getTaskId());
            }
        }

        return schema;
    }

    /**
     * Deterministic fallback when the student has deliverables:
     * Selects the highest-priority incomplete task according to Stage 6 calculation.
     */
    private AIRecommendationResponse executeDeterministicFallback(
            User user,
            List<Task> topTasks,
            Map<Long, TaskPriority> priorityMap
    ) {
        Task topTask = topTasks.get(0);
        TaskPriority priority = priorityMap.get(topTask.getId());

        String scoreStr = (priority != null && priority.getPriorityScore() != null)
                ? priority.getPriorityScore().toString()
                : "High";
        String tierStr = (priority != null && priority.getPriorityLevel() != null)
                ? priority.getPriorityLevel().name()
                : "CRITICAL";

        String headline = "Start with " + topTask.getTitle();
        String reasoning = String.format(
                "Deterministic fallback selected your highest-priority incomplete deliverable (%s, Priority: %s [%s]) due %s.",
                topTask.getTitle(), scoreStr, tierStr, topTask.getDeadline()
        );
        String action = "Work on this task for your next focused study block.";

        AIRecommendation fallback = AIRecommendation.builder()
                .user(user)
                .task(topTask)
                .recommendationType(REC_TYPE_WHAT_SHOULD_I_DO_NOW)
                .recommendationText(headline)
                .aiReasoning(reasoning)
                .suggestedAction(action)
                .generatedAt(LocalDateTime.now())
                .build();

        AIRecommendation saved = aiRecommendationRepository.save(fallback);
        log.info("Persisted deterministic fallback recommendation id: {} for userId: {}", saved.getId(), user.getId());
        return AIRecommendationResponse.fromEntity(saved, true);
    }

    /**
     * Deterministic fallback when the student has zero incomplete deliverables.
     */
    private AIRecommendationResponse executeFallbackNoTasks(User user) {
        AIRecommendation fallback = AIRecommendation.builder()
                .user(user)
                .task(null)
                .recommendationType(REC_TYPE_WHAT_SHOULD_I_DO_NOW)
                .recommendationText("All caught up! No pending deliverables.")
                .aiReasoning("Deterministic fallback verified there are currently no pending or in-progress tasks requiring immediate attention.")
                .suggestedAction("Review upcoming course syllabi or take a well-deserved study break.")
                .generatedAt(LocalDateTime.now())
                .build();

        AIRecommendation saved = aiRecommendationRepository.save(fallback);
        return AIRecommendationResponse.fromEntity(saved, true);
    }
}
