package com.pms.propertymanagement.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCommentDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long propertyId;
    private Long parentCommentId;
    private Long parentReviewId;
    private String content;
    private String status;
    private String rejectionReason;
    private List<PropertyCommentDTO> replies;
    private long helpfulVotes;
    private long unhelpfulVotes;
    private LocalDateTime createdAt;
}
