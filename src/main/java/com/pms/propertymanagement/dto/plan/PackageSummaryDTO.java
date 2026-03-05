package com.pms.propertymanagement.dto.plan;

import com.pms.propertymanagement.dto.OwnerSubscriptionDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PackageSummaryDTO {
    
    public OwnerSubscriptionDTO subscription;
    public List<PropertyPostSummaryDTO> activePosts;
    public List<PropertyPostSummaryDTO> expiringPosts;
    
    @Getter
    @Setter
    public static class PropertyPostSummaryDTO {
        public Long propertyId;
        public String propertyName;
        public String postStatus;
        public String postPackageName;
        public int daysLeft;
        public boolean canRenew;
    }
}