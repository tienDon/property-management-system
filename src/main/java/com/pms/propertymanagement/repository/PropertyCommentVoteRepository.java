package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PropertyCommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyCommentVoteRepository extends JpaRepository<PropertyCommentVote, Long> {
    Optional<PropertyCommentVote> findByCommentIdAndUserId(Long commentId, Long userId);
    long countByCommentIdAndIsHelpful(Long commentId, boolean isHelpful);
}
