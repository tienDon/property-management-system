package com.pms.propertymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsDTO {
    private Double totalRevenue;
    private Double monthlyRevenue;
    private Double targetAchievement;
    private List<ChartDataDTO> revenueChartData;
    private long ownersPurchased;
    private long totalOwners;
    private long totalRooms;
    private long rentedRooms;
    private long availableRooms;
    private long maintenanceRooms;
    private List<AdminActivityDTO> recentTransactions;
}
