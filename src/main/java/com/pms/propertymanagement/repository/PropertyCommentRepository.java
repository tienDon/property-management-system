package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PropertyComment;
import com.pms.propertymanagement.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyCommentRepository extends JpaRepository<PropertyComment, Long> {
    List<PropertyComment> findByPropertyIdAndStatusAndParentCommentIsNullAndParentReviewIsNull(Long propertyId, ReviewStatus status);
    List<PropertyComment> findByStatus(ReviewStatus status);
    List<PropertyComment> findByUserId(Long userId);
}
