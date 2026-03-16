package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    Optional<ReviewVote> findByReviewIdAndUserId(Long reviewId, Long userId);
    long countByReviewIdAndIsHelpful(Long reviewId, Boolean isHelpful);
}
