package com.pms.propertymanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatsDTO {
    private Integer totalProperties;
    private Integer availableProperties; // có thể đăng tin
    private Integer activeSlots;
    private Integer inactiveSlots;
    private Integer activePosts;
}