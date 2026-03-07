package com.pms.propertymanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivePostDTO {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private String postPackageName;
    
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private String status;
    
    private Integer usedBoosts;
    private Long totalViews;
    private Long totalContacts;
    
    private Boolean autoRenew;
    private LocalDateTime lastRenewalDate;
    
    // Computed properties
    private Long daysLeft;
    private Boolean isExpiring; // < 3 days
    private Boolean canUseBoost;
    private String timeLeftText;
}