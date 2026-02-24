package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.enums.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for post analytics and metrics
 * Used for owner dashboard analytics section
 */
@Data
@Builder
public class PostAnalyticsDTO {
    
    private Long postId;
    private String title;
    private String slug;
    
    // Status metrics
    private PostStatus status;
    private boolean isVisible;
    private boolean isExpired;
    private boolean isBoosted;
    
    // Duration metrics
    private LocalDateTime postExpiredAt;
    private LocalDateTime boostExpiredAt;
    private long daysRemaining;
    private long hoursRemaining;
    
    // Engagement metrics
    private Integer viewCount;
    
    // Performance indicators
    private String statusLabel;       // "Đang hiển thị", "Hết hạn", "Đã ẩn"
    private String statusColorClass;  // CSS class for status badge
    
    // Computed fields for UI
    private String formattedExpiry;
    private String formattedBoostExpiry;
}
