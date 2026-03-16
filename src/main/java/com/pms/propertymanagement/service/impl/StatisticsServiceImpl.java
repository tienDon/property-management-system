package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.AdminActivityDTO;
import com.pms.propertymanagement.dto.AdminStatisticsDTO;
import com.pms.propertymanagement.dto.ApiStatisticDTO;
import com.pms.propertymanagement.dto.ChartDataDTO;
import com.pms.propertymanagement.dto.DashboardActivityDTO;
import com.pms.propertymanagement.dto.OwnerStatisticsDTO;
import com.pms.propertymanagement.entity.Payment;
import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.enums.ContractStatus;
import com.pms.propertymanagement.enums.PaymentStatus;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.ApiLogRepository;
import com.pms.propertymanagement.repository.ContractRepository;
import com.pms.propertymanagement.repository.PaymentRepository;
import com.pms.propertymanagement.repository.PostingOrderRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final PostingOrderRepository postingOrderRepository;
    private final ApiLogRepository apiLogRepository;
    private final RoomRepository roomRepository;
    private final com.pms.propertymanagement.repository.UserRepository userRepository;

    @Override
    public List<ApiStatisticDTO> getApiStatistics() {
        return apiLogRepository.getApiStatistics();
    }

    @Override
    public OwnerStatisticsDTO getOwnerStatistics(Long ownerId) {
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfMonth = LocalDateTime.now();

        Double totalIncome = paymentRepository.calculateTotalIncomeByOwnerAndDateRange(
                ownerId,
                PaymentStatus.PAID,
                startOfMonth,
                endOfMonth
        );

        if (totalIncome == null) {
            totalIncome = 0.0;
        }

        Double projectedIncome = contractRepository.sumRentPriceByOwnerAndStatus(ownerId, ContractStatus.ACTIVE);
        if (projectedIncome == null) {
            projectedIncome = 0.0;
        }

        long rentedRooms = contractRepository.countContractsByOwnerAndStatus(ownerId, ContractStatus.ACTIVE);

        long totalRooms = roomRepository.countByOwnerId(ownerId);
        long availableRooms = roomRepository.countByOwnerIdAndStatus(ownerId, RoomStatus.AVAILABLE);
        long maintenanceRooms = roomRepository.countByOwnerIdAndStatus(ownerId, RoomStatus.MAINTENANCE);
        
        long rentedRoomsFromStatus = roomRepository.countByOwnerIdAndStatus(ownerId, RoomStatus.RENTED);

        return new OwnerStatisticsDTO(totalIncome, projectedIncome, rentedRoomsFromStatus, totalRooms, availableRooms, maintenanceRooms);
    }

    @Override
    public List<ChartDataDTO> getOwnerIncomeChart(Long ownerId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(5).withDayOfMonth(1);
        return getOwnerIncomeChart(ownerId, startDate, endDate);
    }

    @Override
    public List<ChartDataDTO> getOwnerIncomeChart(Long ownerId, LocalDate startDate, LocalDate endDate) {
        List<ChartDataDTO> result = new ArrayList<>();
        
        LocalDate current = startDate.withDayOfMonth(1);
        LocalDate end = endDate.withDayOfMonth(endDate.lengthOfMonth());

        while (!current.isAfter(end)) {
            LocalDateTime monthStart = current.atStartOfDay();
            LocalDateTime monthEnd = current.withDayOfMonth(current.lengthOfMonth()).atTime(LocalTime.MAX);
            
            if (monthEnd.isAfter(LocalDateTime.now())) {
                monthEnd = LocalDateTime.now();
            }

            Double monthlyIncome = paymentRepository.calculateTotalIncomeByOwnerAndDateRange(
                ownerId, 
                PaymentStatus.PAID, 
                monthStart, 
                monthEnd
            );
            
            if (monthlyIncome == null) {
                monthlyIncome = 0.0;
            }
            
            String label = current.getMonthValue() + "/" + current.getYear();
            result.add(new ChartDataDTO(label, monthlyIncome));
            
            current = current.plusMonths(1);
        }
        
        return result;
    }

    @Override
    public List<DashboardActivityDTO> getRecentActivities(Long ownerId) {
        List<Payment> payments = paymentRepository.findTop5ByOwnerIdAndPaymentDateLessThanEqualOrderByPaymentDateDesc(ownerId, LocalDateTime.now());
        List<DashboardActivityDTO> activities = new ArrayList<>();
        
        for (Payment payment : payments) {
            String roomName = "N/A";
            String tenantName = "N/A";
            
            if (payment.getContract() != null) {
                if (payment.getContract().getRoom() != null) {
                    roomName = payment.getContract().getRoom().getName();
                }
                if (payment.getContract().getRepresentative() != null) {
                    tenantName = payment.getContract().getRepresentative().getFullName();
                }
            }
            
            activities.add(new DashboardActivityDTO(
                roomName,
                tenantName,
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getPaymentDate()
            ));
        }
        return activities;
    }

    @Override
    public AdminStatisticsDTO getAdminStatistics() {
        Double totalRevenue = postingOrderRepository.calculateTotalRevenueByStatus(PaymentStatus.PAID);
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        LocalDate today = LocalDate.now();

        // Doanh thu theo năm
        LocalDateTime startOfCurrentYear = LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);
        LocalDateTime endOfCurrentYear = LocalDateTime.of(today.withDayOfYear(today.lengthOfYear()), LocalTime.MAX);
        if (endOfCurrentYear.isAfter(LocalDateTime.now())) {
            endOfCurrentYear = LocalDateTime.now();
        }

        LocalDate lastYearDate = today.minusYears(1);
        LocalDateTime startOfLastYear = LocalDateTime.of(lastYearDate.withDayOfYear(1), LocalTime.MIN);
        LocalDateTime endOfLastYear = LocalDateTime.of(lastYearDate.withDayOfYear(lastYearDate.lengthOfYear()), LocalTime.MAX);

        Double currentYearRevenue = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(
            PaymentStatus.PAID,
            startOfCurrentYear,
            endOfCurrentYear
        );
        if (currentYearRevenue == null) {
            currentYearRevenue = 0.0;
        }

        Double lastYearRevenue = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(
            PaymentStatus.PAID,
            startOfLastYear,
            endOfLastYear
        );
        if (lastYearRevenue == null) {
            lastYearRevenue = 0.0;
        }

        Double yearOverYearGrowth;
        if (lastYearRevenue == 0) {
            yearOverYearGrowth = currentYearRevenue > 0 ? 100.0 : 0.0;
        } else {
            yearOverYearGrowth = ((currentYearRevenue - lastYearRevenue) / lastYearRevenue) * 100.0;
        }

        LocalDateTime startOfMonth = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfMonth = LocalDateTime.now();

        Double monthlyRevenue = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(PaymentStatus.PAID, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = 0.0;
        }

        // Doanh thu tháng trước (so sánh tháng này với tháng trước)
        LocalDate firstDayOfThisMonth = today.withDayOfMonth(1);
        LocalDate lastMonthDate = firstDayOfThisMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = LocalDateTime.of(lastMonthDate.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfLastMonth = LocalDateTime.of(lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth()), LocalTime.MAX);

        Double previousMonthRevenue = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(
            PaymentStatus.PAID,
            startOfLastMonth,
            endOfLastMonth
        );
        if (previousMonthRevenue == null) {
            previousMonthRevenue = 0.0;
        }

        Double monthOverMonthGrowth;
        if (previousMonthRevenue == 0) {
            monthOverMonthGrowth = monthlyRevenue > 0 ? 100.0 : 0.0;
        } else {
            monthOverMonthGrowth = ((monthlyRevenue - previousMonthRevenue) / previousMonthRevenue) * 100.0;
        }

        // Hardcoded target for now, e.g., 50,000,000 VND
        double targetRevenue = 50000000.0;
        Double targetAchievement = (monthlyRevenue / targetRevenue) * 100;

        List<ChartDataDTO> revenueChartData = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(5).withDayOfMonth(1);
        
        LocalDate current = startDate.withDayOfMonth(1);
        LocalDate end = endDate.withDayOfMonth(endDate.lengthOfMonth());

        while (!current.isAfter(end)) {
            LocalDateTime monthStart = current.atStartOfDay();
            LocalDateTime monthEnd = current.withDayOfMonth(current.lengthOfMonth()).atTime(LocalTime.MAX);
            
            if (monthEnd.isAfter(LocalDateTime.now())) {
                monthEnd = LocalDateTime.now();
            }

            Double monthIncome = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(
                PaymentStatus.PAID, 
                monthStart, 
                monthEnd
            );
            
            if (monthIncome == null) {
                monthIncome = 0.0;
            }
            
            String label = current.getMonthValue() + "/" + current.getYear();
            revenueChartData.add(new ChartDataDTO(label, monthIncome));
            
            current = current.plusMonths(1);
        }

        long ownersPurchased = postingOrderRepository.countDistinctOwnersPurchasedPackagesByStatus(PaymentStatus.PAID);
        long totalOwners = userRepository.countByRoles_Name("ROLE_OWNER");
        
        long totalRooms = roomRepository.count();
        long rentedRooms = roomRepository.countByStatus(RoomStatus.RENTED);
        long availableRooms = roomRepository.countByStatus(RoomStatus.AVAILABLE);
        long maintenanceRooms = roomRepository.countByStatus(RoomStatus.MAINTENANCE);

        List<PostingOrder> recentOrders = postingOrderRepository.findTop5ByStatusAndPaidAtLessThanEqualOrderByPaidAtDesc(PaymentStatus.PAID, LocalDateTime.now());
        List<AdminActivityDTO> recentTransactions = recentOrders.stream()
            .map(order -> new AdminActivityDTO(
                order.getOwner().getFullName(),
                order.getPostingPackage().getName(),
                (double) order.getAmount(),
                order.getPaidAt(),
                order.getStatus().name()
            ))
            .collect(Collectors.toList());

        return new AdminStatisticsDTO(
            totalRevenue,
            currentYearRevenue,
            lastYearRevenue,
            yearOverYearGrowth,
            monthlyRevenue,
            previousMonthRevenue,
            monthOverMonthGrowth,
            targetAchievement,
            revenueChartData,
            ownersPurchased,
            totalOwners,
            totalRooms,
            rentedRooms,
            availableRooms,
            maintenanceRooms,
            recentTransactions
        );
    }
}
