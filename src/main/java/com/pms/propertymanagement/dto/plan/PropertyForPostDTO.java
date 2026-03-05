package com.pms.propertymanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyForPostDTO {
    private Long id;
    private String name;
    private String address;
    private Integer availableRooms;
    private Integer totalRooms;
    private Boolean hasActiveSlot;
    private Boolean hasActivePost;
    private String propertyType;
    private BigDecimal monthlyRent;
    
    public Boolean isEligibleForPost() {
        return hasActiveSlot && !hasActivePost;
    }
}