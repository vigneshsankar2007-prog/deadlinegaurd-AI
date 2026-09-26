package com.deadlineguard.repository;

import com.deadlineguard.entity.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AIRecommendation entity queries.
 */
@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

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
