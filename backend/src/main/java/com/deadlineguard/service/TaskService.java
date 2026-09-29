package com.deadlineguard.service;

import com.deadlineguard.dto.task.CreateTaskRequest;
import com.deadlineguard.dto.task.TaskResponse;
import com.deadlineguard.dto.task.UpdateTaskRequest;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.entity.enums.TaskStatus;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.SubjectRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing academic deliverables and tasks with strict student-level ownership enforcement.
 *
 * STAGE BOUNDARY: Zero priority calculations, Gemini API calls, study plans, or notifications
 * are performed here. Those capabilities belong strictly to future stages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    /**
     * Create a new task strictly associated with the authenticated student.
     * If subjectId is supplied, validates that the subject also belongs to the student.
     */
    @Transactional
    public TaskResponse createTask(Long userId, CreateTaskRequest request) {
        log.info("Creating task '{}' for userId: {}", request.getTitle(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndUserId(request.getSubjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));
        }

        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING;
        LocalDateTime completedAt = (status == TaskStatus.COMPLETED) ? LocalDateTime.now() : null;

        Task task = Task.builder()
                .user(user)
                .subject(subject)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .taskType(request.getTaskType())
                .deadline(request.getDeadline())
                .difficulty(request.getDifficulty())
                .academicWeight(request.getAcademicWeight())
                .estimatedHours(request.getEstimatedHours())
                .status(status)
                .completedAt(completedAt)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    /**
     * Retrieve all tasks belonging exclusively to the authenticated student,
     * with optional filtering by status and subject.
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long userId, TaskStatus status, Long subjectId) {
        List<Task> tasks;

        if (subjectId != null && status != null) {
            tasks = taskRepository.findByUserIdAndSubjectIdAndStatusOrderByDeadlineAsc(userId, subjectId, status);
        } else if (subjectId != null) {
            tasks = taskRepository.findByUserIdAndSubjectIdOrderByDeadlineAsc(userId, subjectId);
        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatusOrderByDeadlineAsc(userId, status);
        } else {
            tasks = taskRepository.findByUserIdOrderByDeadlineAsc(userId);
        }

        return tasks.stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific task by ID only if it belongs to the authenticated student.
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Long userId) {
        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return TaskResponse.fromEntity(task);
    }

    /**
     * Update a task owned by the authenticated student.
     * Ownership cannot be changed. If subjectId changes, validates ownership of the new subject.
     */
    @Transactional
    public TaskResponse updateTask(Long id, Long userId, UpdateTaskRequest request) {
        log.info("Updating task id: {} for userId: {}", id, userId);

        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findByIdAndUserId(request.getSubjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));
            task.setSubject(subject);
        } else {
            task.setSubject(null);
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setDeadline(request.getDeadline());
        task.setDifficulty(request.getDifficulty());
        task.setAcademicWeight(request.getAcademicWeight());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setStatus(request.getStatus());

        // Strictly enforce DB check constraint: completed_at set iff status is COMPLETED
        if (request.getStatus() == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
        }

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    /**
     * Delete a task owned by the authenticated student.
     */
    @Transactional
    public void deleteTask(Long id, Long userId) {
        log.info("Deleting task id: {} for userId: {}", id, userId);

        Task task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        taskRepository.delete(task);
    }
}
