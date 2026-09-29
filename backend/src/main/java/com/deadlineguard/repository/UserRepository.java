package com.deadlineguard.repository;

import com.deadlineguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity CRUD and student credential queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find student by unique email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email address is already registered.
     */
    boolean existsByEmail(String email);
}
