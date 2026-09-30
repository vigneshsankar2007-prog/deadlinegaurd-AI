package com.deadlineguard.repository;

import com.deadlineguard.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StudySession entity queries and focus timer tracking.
 */
@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    /**
     * Find a study session only if it belongs to the given student.
     */
    Optional<StudySession> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all study sessions for a student ordered by newest first.
     */
    List<StudySession> findByUserIdOrderByStartTimeDesc(Long userId);

    /**
     * Find study sessions for a student filtered by completion status.
     */
    List<StudySession> findByUserIdAndIsCompletedOrderByStartTimeDesc(Long userId, Boolean isCompleted);

    /**
     * Find study sessions for a student tied to a specific task.
     */
    List<StudySession> findByUserIdAndTaskIdOrderByStartTimeDesc(Long userId, Long taskId);

    /**
     * Find all study sessions tied to a specific task.
     */
    List<StudySession> findByTaskIdOrderByStartTimeDesc(Long taskId);

    /**
     * Find currently active (uncompleted) session for a student.
     */
    Optional<StudySession> findByUserIdAndIsCompletedFalse(Long userId);

    /**
     * Sum total focus minutes completed by a student between two timestamps.
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s " +
           "WHERE s.user.id = :userId " +
           "AND s.isCompleted = true " +
           "AND s.startTime >= :start AND s.startTime < :end")
    int sumDurationMinutesForPeriod(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
