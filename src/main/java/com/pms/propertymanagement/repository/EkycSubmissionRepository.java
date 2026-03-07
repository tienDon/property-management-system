package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.EkycSubmission;
import com.pms.propertymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EkycSubmissionRepository extends JpaRepository<EkycSubmission, Long> {
    Optional<EkycSubmission> findTopByUserOrderByCreatedAtDesc(User user);
}

