package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.enums.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for owner's post dashboard
 * Shows post marketing content with visibility status
 */
@Data
@Builder
public class PostOwnerResponse {
    
    private Long postId;
    private Long propertyId;
    
    // Marketing content (from Post)
    private String title;
    private String slug;
    private String description;
    
    // Property reference (from Property)
    private String propertyName;  // Internal property name
    private String addressNumber;
    private String wardName;
    private String provinceName;
    private String imageUrl;
    
    // Visibility status
    private PostStatus status;
    private LocalDateTime postExpiredAt;
    private LocalDateTime boostExpiredAt;
    
    // Staff review feedback
    private String rejectionReason;
    
    // Analytics
    private Integer viewCount;
    
    // Computed fields
    private boolean isExpired;
    private boolean isBoosted;
    private boolean isActive;
    private long daysRemaining;  // Days until expiration
    
    // Formatted dates for display
    private String formattedExpiry;
    private String formattedCreatedAt;
}
