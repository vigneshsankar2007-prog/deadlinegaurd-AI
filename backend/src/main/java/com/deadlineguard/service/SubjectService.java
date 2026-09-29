package com.deadlineguard.service;

import com.deadlineguard.dto.subject.CreateSubjectRequest;
import com.deadlineguard.dto.subject.SubjectResponse;
import com.deadlineguard.dto.subject.UpdateSubjectRequest;
import com.deadlineguard.entity.Subject;
import com.deadlineguard.entity.Task;
import com.deadlineguard.entity.User;
import com.deadlineguard.exception.DuplicateResourceException;
import com.deadlineguard.exception.ResourceNotFoundException;
import com.deadlineguard.repository.SubjectRepository;
import com.deadlineguard.repository.TaskRepository;
import com.deadlineguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing enrolled academic course subjects with student-level ownership enforcement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Create a new subject strictly associated with the authenticated student.
     */
    @Transactional
    public SubjectResponse createSubject(Long userId, CreateSubjectRequest request) {
        log.info("Creating subject '{}' for userId: {}", request.getSubjectCode(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String code = request.getSubjectCode().trim().toUpperCase();
        String name = request.getSubjectName().trim();

        if (subjectRepository.existsByUserIdAndSubjectCode(userId, code)) {
            throw new DuplicateResourceException("Subject with course code '" + code + "' already exists in your enrollment");
        }

        if (subjectRepository.existsByUserIdAndSubjectName(userId, name)) {
            throw new DuplicateResourceException("Subject with name '" + name + "' already exists in your enrollment");
        }

        Subject subject = Subject.builder()
                .user(user)
                .subjectName(name)
                .subjectCode(code)
                .credits(request.getCredits())
                .colorHex(request.getColorHex().trim().toUpperCase())
                .build();

        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.fromEntity(saved);
    }

    /**
     * Retrieve all subjects belonging exclusively to the authenticated student.
     */
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjects(Long userId) {
        return subjectRepository.findByUserId(userId)
                .stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a specific subject only if it belongs to the authenticated student.
     */
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id, Long userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        return SubjectResponse.fromEntity(subject);
    }

    /**
     * Update a subject owned by the authenticated student. Ownership cannot be transferred.
     */
    @Transactional
    public SubjectResponse updateSubject(Long id, Long userId, UpdateSubjectRequest request) {
        log.info("Updating subject id: {} for userId: {}", id, userId);

        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        String code = request.getSubjectCode().trim().toUpperCase();
        String name = request.getSubjectName().trim();

        if (subjectRepository.existsByUserIdAndSubjectCodeAndIdNot(userId, code, id)) {
            throw new DuplicateResourceException("Subject with course code '" + code + "' already exists in your enrollment");
        }

        if (subjectRepository.existsByUserIdAndSubjectNameAndIdNot(userId, name, id)) {
            throw new DuplicateResourceException("Subject with name '" + name + "' already exists in your enrollment");
        }

        subject.setSubjectName(name);
        subject.setSubjectCode(code);
        subject.setCredits(request.getCredits());
        subject.setColorHex(request.getColorHex().trim().toUpperCase());

        Subject updated = subjectRepository.save(subject);
        return SubjectResponse.fromEntity(updated);
    }

    /**
     * Delete a subject owned by the authenticated student.
     * Related tasks are unlinked (subject_id becomes null), preserving student work.
     */
    @Transactional
    public void deleteSubject(Long id, Long userId) {
        log.info("Deleting subject id: {} for userId: {}", id, userId);

        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        // Preserve tasks: unlink subject reference in memory & DB before deleting subject
        List<Task> relatedTasks = taskRepository.findBySubjectId(id);
        if (!relatedTasks.isEmpty()) {
            for (Task task : relatedTasks) {
                task.setSubject(null);
            }
            taskRepository.saveAll(relatedTasks);
        }

        subjectRepository.delete(subject);
    }
}
