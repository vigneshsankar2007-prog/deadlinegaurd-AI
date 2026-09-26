package com.deadlineguard.repository;

import com.deadlineguard.entity.ProductivityStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for daily ProductivityStat entity queries and analytics aggregations.
 */
@Repository
public interface ProductivityStatRepository extends JpaRepository<ProductivityStat, Long> {

    /**
     * Find metric record for a student on a specific calendar date.
     */
    Optional<ProductivityStat> findByUserIdAndStatDate(Long userId, LocalDate statDate);

    /**
     * Find historical productivity metrics for a student sorted chronologically descending.
     */
    List<ProductivityStat> findByUserIdOrderByStatDateDesc(Long userId);

    /**
     * Find metric entries for a student within a date range (e.g. last 7 days, 30 days) ordered ascending.
     */
    List<ProductivityStat> findByUserIdAndStatDateBetweenOrderByStatDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
