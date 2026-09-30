package com.deadlineguard.repository;

import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task entity operations and academic query filters.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks owned by a student, sorted chronologically by deadline.
     */
    List<Task> findByUserIdOrderByDeadlineAsc(Long userId);

    /**
     * Find student tasks filtered by current lifecycle status.
     */
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    
    List<Task> findByUserIdAndSubjectIdAndStatusOrderByDeadlineAsc(Long userId, Long subjectId, TaskStatus status);
    List<Task> findByUserIdAndSubjectIdOrderByDeadlineAsc(Long userId, Long subjectId);
    List<Task> findByUserIdAndStatusOrderByDeadlineAsc(Long userId, TaskStatus status);

    /**
     * Find student tasks with deadline within a specific time window.
     */
    List<Task> findByUserIdAndDeadlineBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find task by ID and owner student ID (prevents cross-tenant access).
     */
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all tasks belonging to a specific course subject.
     */
    List<Task> findBySubjectId(Long subjectId);

    /**
     * Pure database count retrieval primitive: counts non-completed tasks for a student
     * within a specified time window (e.g. 72-hour window +/- 36h from task deadline).
     *
     * IMPORTANT STAGE BOUNDARY: This query performs raw counting only and contains NO scoring
     * or weighting logic. The formula (workload_score = min(100, count * 20)) belongs strictly
     * to the PriorityCalculationService in Stage 6.
     */
    @Query("SELECT COUNT(t) FROM Task t " +
           "WHERE t.user.id = :userId " +
           "AND t.id <> :excludeTaskId " +
           "AND t.status IN ('PENDING', 'IN_PROGRESS') " +
           "AND t.deadline BETWEEN :windowStart AND :windowEnd")
    long countConcurrentWorkloadTasks(
            @Param("userId") Long userId,
            @Param("excludeTaskId") Long excludeTaskId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );
}
