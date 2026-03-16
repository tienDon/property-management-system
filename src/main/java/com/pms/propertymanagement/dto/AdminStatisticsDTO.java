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
    private Double currentYearRevenue;
    private Double lastYearRevenue;
    private Double yearOverYearGrowth;
    private Double monthlyRevenue;
    private Double previousMonthRevenue;
    private Double monthOverMonthGrowth;
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
