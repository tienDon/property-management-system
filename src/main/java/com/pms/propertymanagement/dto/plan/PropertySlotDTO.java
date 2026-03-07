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
public class PropertySlotDTO {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private String propertyAddress;
    private Boolean isActive;
    private String deactivationReason;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
}