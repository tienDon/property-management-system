package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.response.PropertyCommentDTO;
import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.enums.ReviewStatus;
import com.pms.propertymanagement.enums.ReportStatus;
import com.pms.propertymanagement.repository.*;
import com.pms.propertymanagement.service.PropertyCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyCommentServiceImpl implements PropertyCommentService {

    private final PropertyCommentRepository commentRepository;
    private final PropertyCommentVoteRepository voteRepository;
    private final PropertyCommentReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public PropertyCommentDTO createComment(PropertyCommentDTO commentDTO) {
        User user = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Property property = propertyRepository.findById(commentDTO.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        PropertyComment comment = PropertyComment.builder()
                .user(user)
                .property(property)
                .content(sanitizeText(commentDTO.getContent()))
                .status(ReviewStatus.PENDING)
                .build();

        if (commentDTO.getParentCommentId() != null) {
            PropertyComment parent = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        if (commentDTO.getParentReviewId() != null) {
            Review parentReview = reviewRepository.findById(commentDTO.getParentReviewId())
                    .orElseThrow(() -> new RuntimeException("Parent review not found"));
            comment.setParentReview(parentReview);
        }

        PropertyComment savedComment = commentRepository.save(comment);
        return mapToDTO(savedComment);
    }

    @Override
    @Transactional
    public void approveComment(Long commentId) {
        PropertyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus(ReviewStatus.APPROVED);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void rejectComment(Long commentId, String reason) {
        PropertyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus(ReviewStatus.REJECTED);
        comment.setRejectionReason(reason);
        commentRepository.save(comment);
    }

    @Override
    public List<PropertyCommentDTO> getCommentsByProperty(Long propertyId) {
        return commentRepository.findByPropertyIdAndStatusAndParentCommentIsNullAndParentReviewIsNull(propertyId, ReviewStatus.APPROVED)
                .stream().map(this::mapToDTOWithReplies).collect(Collectors.toList());
    }

    @Override
    public List<PropertyCommentDTO> getPendingComments() {
        return commentRepository.findByStatus(ReviewStatus.PENDING)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<PropertyCommentDTO> getCommentsByUser(Long userId) {
        return commentRepository.findByUserId(userId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void voteComment(Long commentId, Long userId, boolean helpful) {
        PropertyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PropertyCommentVote vote = voteRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(PropertyCommentVote.builder().comment(comment).user(user).build());
        
        vote.setIsHelpful(helpful);
        voteRepository.save(vote);
    }

    @Override
    @Transactional
    public void reportComment(Long commentId, Long userId, String reason) {
        PropertyComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PropertyCommentReport report = PropertyCommentReport.builder()
                .comment(comment)
                .reporter(user)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();
        reportRepository.save(report);
    }

    private String sanitizeText(String text) {
        if (text == null) return null;
        return text.replaceAll("<[^>]*>", "").trim();
    }

    private PropertyCommentDTO mapToDTO(PropertyComment comment) {
        return PropertyCommentDTO.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .propertyId(comment.getProperty().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .parentReviewId(comment.getParentReview() != null ? comment.getParentReview().getId() : null)
                .content(comment.getContent())
                .status(comment.getStatus().name())
                .rejectionReason(comment.getRejectionReason())
                .helpfulVotes(voteRepository.countByCommentIdAndIsHelpful(comment.getId(), true))
                .unhelpfulVotes(voteRepository.countByCommentIdAndIsHelpful(comment.getId(), false))
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private PropertyCommentDTO mapToDTOWithReplies(PropertyComment comment) {
        PropertyCommentDTO dto = mapToDTO(comment);
        dto.setReplies(comment.getReplies().stream()
                .filter(r -> r.getStatus() == ReviewStatus.APPROVED)
                .map(this::mapToDTOWithReplies)
                .collect(Collectors.toList()));
        return dto;
    }
}
