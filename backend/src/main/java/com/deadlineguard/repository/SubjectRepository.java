package com.deadlineguard.repository;

import com.deadlineguard.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subject entity operations.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Find all subjects enrolled by a specific student.
     */
    List<Subject> findByUserId(Long userId);

    /**
     * Find a subject by owner student ID and course catalog code.
     */
    Optional<Subject> findByUserIdAndSubjectCode(Long userId, String subjectCode);

    /**
     * Check if a subject code already exists for a student.
     */
    boolean existsByUserIdAndSubjectCode(Long userId, String subjectCode);

    /**
     * Check if a subject name already exists for a student.
     */
    boolean existsByUserIdAndSubjectName(Long userId, String subjectName);

    /**
     * Check if a subject code already exists for a student excluding a specific subject ID (for updates).
     */
    boolean existsByUserIdAndSubjectCodeAndIdNot(Long userId, String subjectCode, Long id);

    /**
     * Check if a subject name already exists for a student excluding a specific subject ID (for updates).
     */
    boolean existsByUserIdAndSubjectNameAndIdNot(Long userId, String subjectName, Long id);

    /**
     * Find subject by ID and student owner ID (for ownership verification).
     */
    Optional<Subject> findByIdAndUserId(Long id, Long userId);
}
