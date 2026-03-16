package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.request.ReviewDTO;
import java.util.List;

public interface ReviewService {
    ReviewDTO createReview(ReviewDTO reviewDTO);
    void approveReview(Long reviewId);
    void rejectReview(Long reviewId, String reason);
    List<ReviewDTO> getReviewsByProperty(Long propertyId);
    List<ReviewDTO> getPendingReviews();
    void voteReview(Long reviewId, Long userId, boolean helpful);
    void replyReview(Long reviewId, Long landlordId, String content);
    void updateLandlordRating(Long landlordId);
    ReviewDTO getReviewByContractAndTenant(Long contractId, Long tenantId);
    void reportReview(Long reviewId, Long userId, String reason);
    com.pms.propertymanagement.dto.response.StarDistributionDTO getStarDistribution(Long propertyId);
}
