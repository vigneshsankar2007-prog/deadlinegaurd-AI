package com.deadlineguard.repository;

import com.deadlineguard.entity.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AIRecommendation entity queries.
 */
@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    /**
     * Find a specific recommendation only if it belongs to the given student.
     */
    Optional<AIRecommendation> findByIdAndUserId(Long id, Long userId);

    /**
     * Find all recommendations generated for a student, ordered newest first.
     */
    List<AIRecommendation> findByUserIdOrderByGeneratedAtDesc(Long userId);

    /**
     * Find latest N recommendations for student dashboard preview.
     */
    List<AIRecommendation> findTop5ByUserIdOrderByGeneratedAtDesc(Long userId);

    /**
     * Find recommendations generated for a specific task.
     */
    List<AIRecommendation> findByTaskIdOrderByGeneratedAtDesc(Long taskId);
}
