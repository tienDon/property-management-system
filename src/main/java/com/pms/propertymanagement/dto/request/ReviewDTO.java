package com.pms.propertymanagement.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long landlordId;
    private Long propertyId;
    private Long contractId;
    
    // Ratings
    private Integer hygieneRating;
    private Integer attitudeRating;
    private Integer utilitiesRating;
    private Integer safetyRating;
    private Integer priceRating;
    private Double averageRating;
    
    private String comment;
    private String status;
    private String rejectionReason;
    
    private List<String> imageUrls;
    private List<MultipartFile> images; // For upload
    
    private String landlordReply;
    private Long helpfulVotes;
    private Long unhelpfulVotes;
    
    private LocalDateTime createdAt;
}
