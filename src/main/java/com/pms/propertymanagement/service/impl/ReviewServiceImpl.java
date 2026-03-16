package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ReviewDTO;
import com.pms.propertymanagement.dto.response.CloudinaryResponse;
import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.enums.ReportStatus;
import com.pms.propertymanagement.enums.ReviewStatus;
import com.pms.propertymanagement.repository.*;
import com.pms.propertymanagement.service.CloudinaryService;
import com.pms.propertymanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final LandlordRatingRepository landlordRatingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        // Text sanitization
        String sanitizedComment = sanitizeText(reviewDTO.getComment());
        
        User tenant = userRepository.findById(reviewDTO.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Property property = propertyRepository.findById(reviewDTO.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));
        Contract contract = contractRepository.findById(reviewDTO.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        User landlord = property.getOwner();

        // Unique constraint check (handled by DB as well, but good to check)
        reviewRepository.findByContractIdAndTenantId(contract.getId(), tenant.getId())
                .ifPresent(r -> { throw new RuntimeException("You have already reviewed this lease"); });

        Review review = Review.builder()
                .tenant(tenant)
                .landlord(landlord)
                .property(property)
                .contract(contract)
                .hygieneRating(reviewDTO.getHygieneRating())
                .attitudeRating(reviewDTO.getAttitudeRating())
                .utilitiesRating(reviewDTO.getUtilitiesRating())
                .safetyRating(reviewDTO.getSafetyRating())
                .priceRating(reviewDTO.getPriceRating())
                .comment(sanitizedComment)
                .status(ReviewStatus.PENDING)
                .build();

        review.calculateAverageRating();
        
        Review savedReview = reviewRepository.save(review);

        // Handle images (max 5)
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            int count = 0;
            for (MultipartFile file : reviewDTO.getImages()) {
                if (count >= 5) break;
                if (!file.isEmpty()) {
                    CloudinaryResponse response = cloudinaryService.uploadImage(file, "reviews");
                    ReviewImage image = ReviewImage.builder()
                            .review(savedReview)
                            .imageUrl(response.getUrl())
                            .order(count)
                            .build();
                    savedReview.getImages().add(image);
                    count++;
                }
            }
        }

        return mapToDTO(savedReview);
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        
        // Update landlord rating
        updateLandlordRating(review.getLandlord().getId());
    }

    @Override
    @Transactional
    public void rejectReview(Long reviewId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectionReason(reason);
        reviewRepository.save(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByProperty(Long propertyId) {
        return reviewRepository.findByPropertyIdAndStatus(propertyId, ReviewStatus.APPROVED)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void voteReview(Long reviewId, Long userId, boolean helpful) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReviewVote vote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElse(ReviewVote.builder().review(review).user(user).build());
        
        vote.setIsHelpful(helpful);
        reviewVoteRepository.save(vote);
    }

    @Override
    @Transactional
    public void replyReview(Long reviewId, Long landlordId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));

        if (!review.getLandlord().getId().equals(landlordId)) {
            throw new RuntimeException("Only the landlord can reply to this review");
        }

        ReviewReply reply = reviewReplyRepository.findByReviewId(reviewId)
                .orElse(ReviewReply.builder().review(review).landlord(landlord).build());
        
        reply.setContent(sanitizeText(content));
        reviewReplyRepository.save(reply);
    }

    @Override
    @Transactional
    public void updateLandlordRating(Long landlordId) {
        List<Review> approvedReviews = reviewRepository.findByLandlordIdAndStatus(landlordId, ReviewStatus.APPROVED);
        
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));
        
        LandlordRating rating = landlordRatingRepository.findByLandlordId(landlordId)
                .orElse(LandlordRating.builder().landlord(landlord).build());

        if (approvedReviews.isEmpty()) {
            rating.setAverageRating(0.0);
            rating.setTotalReviews(0L);
            landlordRatingRepository.save(rating);
            return;
        }

        double totalAvg = 0, totalHygiene = 0, totalAttitude = 0, totalUtilities = 0, totalSafety = 0, totalPrice = 0;
        for (Review r : approvedReviews) {
            totalAvg += r.getAverageRating();
            totalHygiene += r.getHygieneRating();
            totalAttitude += r.getAttitudeRating();
            totalUtilities += r.getUtilitiesRating();
            totalSafety += r.getSafetyRating();
            totalPrice += r.getPriceRating();
        }

        int size = approvedReviews.size();
        rating.setAverageRating(totalAvg / size);
        rating.setHygieneAverage(totalHygiene / size);
        rating.setAttitudeAverage(totalAttitude / size);
        rating.setUtilitiesAverage(totalUtilities / size);
        rating.setSafetyAverage(totalSafety / size);
        rating.setPriceAverage(totalPrice / size);
        rating.setTotalReviews((long) size);

        landlordRatingRepository.save(rating);
    }

    @Override
    public ReviewDTO getReviewByContractAndTenant(Long contractId, Long tenantId) {
        return reviewRepository.findByContractIdAndTenantId(contractId, tenantId)
                .map(this::mapToDTO).orElse(null);
    }

    @Override
    @Transactional
    public void reportReview(Long reviewId, Long userId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReviewReport report = ReviewReport.builder()
                .review(review)
                .reporter(user)
                .reason(sanitizeText(reason))
                .status(ReportStatus.PENDING)
                .build();
        
        // You might want to add ReviewReportRepository to the fields
        reviewReportRepository.save(report);
    }

    @Override
    public com.pms.propertymanagement.dto.response.StarDistributionDTO getStarDistribution(Long propertyId) {
        List<Review> reviews = reviewRepository.findByPropertyIdAndStatus(propertyId, ReviewStatus.APPROVED);
        long one = 0, two = 0, three = 0, four = 0, five = 0;
        for (Review r : reviews) {
            double avg = r.getAverageRating();
            if (avg >= 4.5) five++;
            else if (avg >= 3.5) four++;
            else if (avg >= 2.5) three++;
            else if (avg >= 1.5) two++;
            else one++;
        }
        return com.pms.propertymanagement.dto.response.StarDistributionDTO.builder()
                .oneStar(one).twoStars(two).threeStars(three).fourStars(four).fiveStars(five)
                .totalReviews(reviews.size())
                .build();
    }

    private String sanitizeText(String text) {
        if (text == null) return null;
        // Basic HTML sanitization to prevent XSS
        return text.replaceAll("<[^>]*>", "")
                   .replaceAll("(?i)javascript:", "")
                   .replaceAll("(?i)onload", "")
                   .trim();
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .tenantId(review.getTenant().getId())
                .tenantName(review.getTenant().getFullName())
                .landlordId(review.getLandlord().getId())
                .propertyId(review.getProperty().getId())
                .contractId(review.getContract().getId())
                .hygieneRating(review.getHygieneRating())
                .attitudeRating(review.getAttitudeRating())
                .utilitiesRating(review.getUtilitiesRating())
                .safetyRating(review.getSafetyRating())
                .priceRating(review.getPriceRating())
                .averageRating(review.getAverageRating())
                .comment(review.getComment())
                .status(review.getStatus().name())
                .rejectionReason(review.getRejectionReason())
                .imageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).collect(Collectors.toList()))
                .landlordReply(review.getReply() != null ? review.getReply().getContent() : null)
                .helpfulVotes(reviewVoteRepository.countByReviewIdAndIsHelpful(review.getId(), true))
                .unhelpfulVotes(reviewVoteRepository.countByReviewIdAndIsHelpful(review.getId(), false))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
