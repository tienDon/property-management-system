package com.pms.propertymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerStatisticsDTO {
    private Double totalIncome; // Thực thu (từ Payments)
    private Double projectedIncome; // Dự kiến (từ Contracts)
    private long rentedRooms;
    
    // New fields for Pie Chart
    private long totalRooms;
    private long availableRooms;
    private long maintenanceRooms;
}
