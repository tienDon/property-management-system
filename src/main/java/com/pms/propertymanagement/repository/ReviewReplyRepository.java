package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Optional<ReviewReply> findByReviewId(Long reviewId);
}
