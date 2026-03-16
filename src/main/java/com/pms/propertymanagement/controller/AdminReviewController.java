package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ReviewDTO;
import com.pms.propertymanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping("/pending")
    public ResponseEntity<List<ReviewDTO>> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(@PathVariable Long reviewId, @RequestParam String reason) {
        reviewService.rejectReview(reviewId, reason);
        return ResponseEntity.ok().build();
    }
}
