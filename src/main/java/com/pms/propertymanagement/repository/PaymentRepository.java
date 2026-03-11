package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Payment;
import com.pms.propertymanagement.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.owner.id = :ownerId " +
           "AND p.status = :status " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate")
    Double calculateTotalIncomeByOwnerAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT new com.pms.propertymanagement.dto.ChartDataDTO(" +
           "CONCAT(FUNCTION('MONTH', p.paymentDate), '/', FUNCTION('YEAR', p.paymentDate)), " +
           "SUM(p.amount)) " +
           "FROM Payment p " +
           "WHERE p.owner.id = :ownerId " +
           "AND p.status = :status " +
           "AND p.paymentDate >= :startDate " +
           "GROUP BY FUNCTION('YEAR', p.paymentDate), FUNCTION('MONTH', p.paymentDate) " +
           "ORDER BY FUNCTION('YEAR', p.paymentDate), FUNCTION('MONTH', p.paymentDate)")
    java.util.List<com.pms.propertymanagement.dto.ChartDataDTO> findMonthlyIncome(
            @Param("ownerId") Long ownerId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate
    );
    
    java.util.List<Payment> findTop5ByOwnerIdOrderByPaymentDateDesc(Long ownerId);

    java.util.List<Payment> findTop5ByOwnerIdAndPaymentDateLessThanEqualOrderByPaymentDateDesc(Long ownerId, LocalDateTime date);
}
