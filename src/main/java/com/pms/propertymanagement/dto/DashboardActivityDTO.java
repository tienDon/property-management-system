package com.pms.propertymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDTO {
    private String roomName;
    private String tenantName;
    private String status;
    private Double amount;
    private LocalDateTime date;
}
