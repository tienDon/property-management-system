package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.AdminStatisticsDTO;
import com.pms.propertymanagement.dto.ApiStatisticDTO;
import com.pms.propertymanagement.dto.ChartDataDTO;
import com.pms.propertymanagement.dto.DashboardActivityDTO;
import com.pms.propertymanagement.dto.OwnerStatisticsDTO;
import com.pms.propertymanagement.entity.Payment;
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

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final PostingOrderRepository postingOrderRepository;
    private final ApiLogRepository apiLogRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<ApiStatisticDTO> getApiStatistics() {
        return apiLogRepository.getApiStatistics();
    }

    @Override
    public OwnerStatisticsDTO getOwnerStatistics(Long ownerId) {
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()), LocalTime.MAX);

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
        List<Payment> payments = paymentRepository.findTop5ByOwnerIdOrderByPaymentDateDesc(ownerId);
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

        long ownersPurchased = postingOrderRepository.countDistinctOwnersPurchasedPackagesByStatus(PaymentStatus.PAID);

        return new AdminStatisticsDTO(totalRevenue, ownersPurchased);
    }
}
