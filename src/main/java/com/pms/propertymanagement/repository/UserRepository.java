package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    User findByFullName(String fullName);
    Optional<User> findById(Long id);

    // === PESSIMISTIC LOCKING FOR CONCURRENCY CONTROL ===
    /**
     * Find user by ID with pessimistic write lock
     * ENTERPRISE-GRADE: Includes timeout and proper isolation
     * Used in subscription creation to prevent race conditions
     * Locks the User aggregate root during subscription operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000"),  // 10 second timeout
        @QueryHint(name = "org.hibernate.readOnly", value = "false")
    })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
}
