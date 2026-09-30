package com.deadlineguard.repository;

import com.deadlineguard.entity.TaskPriority;
import com.deadlineguard.entity.enums.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TaskPriority entity queries.
 */
@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriority, Long> {

    /**
     * Find calculated priority breakdown for a specific task.
     */
    Optional<TaskPriority> findByTaskId(Long taskId);

    /**
     * Find calculated priority breakdown for a task only if it belongs to the given user.
     */
    Optional<TaskPriority> findByTaskIdAndTaskUserId(Long taskId, Long userId);

    /**
     * Find all task priorities for a student, ordered by highest composite score first.
     */
    List<TaskPriority> findByTaskUserIdOrderByPriorityScoreDesc(Long userId);

    /**
     * Find all task priorities for a student, ordered by earliest task deadline first.
     */
    List<TaskPriority> findByTaskUserIdOrderByTaskDeadlineAsc(Long userId);

    /**
     * Find task priorities for a student filtered by priority level.
     */
    List<TaskPriority> findByTaskUserIdAndPriorityLevel(Long userId, PriorityLevel priorityLevel);
}
