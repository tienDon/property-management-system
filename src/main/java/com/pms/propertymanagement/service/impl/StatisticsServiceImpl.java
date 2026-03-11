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

        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfMonth = LocalDateTime.now();

        Double monthlyRevenue = postingOrderRepository.calculateTotalRevenueByStatusAndDateRange(PaymentStatus.PAID, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = 0.0;
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
            monthlyRevenue,
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
