package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ReviewDTO;
import com.pms.propertymanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@ModelAttribute ReviewDTO reviewDTO) {
        return ResponseEntity.ok(reviewService.createReview(reviewDTO));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewDTO>> getPropertyReviews(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getReviewsByProperty(propertyId));
    }

    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<Void> voteReview(@PathVariable Long reviewId, @RequestParam Long userId, @RequestParam boolean helpful) {
        reviewService.voteReview(reviewId, userId, helpful);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<Void> replyReview(@PathVariable Long reviewId, @RequestParam Long landlordId, @RequestParam String content) {
        reviewService.replyReview(reviewId, landlordId, content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<Void> reportReview(@PathVariable Long reviewId, @RequestParam Long userId, @RequestParam String reason) {
        reviewService.reportReview(reviewId, userId, reason);
        return ResponseEntity.ok().build();
    }
}
