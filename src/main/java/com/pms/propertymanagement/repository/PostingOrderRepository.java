package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface PostingOrderRepository extends JpaRepository<PostingOrder, Long> {

    Optional<PostingOrder> findByVnpTxnRef(String vnpTxnRef);

    boolean existsByOwner_IdAndStatusAndRemainingUsesGreaterThan(
            Long ownerId,
            PaymentStatus status,
            int minRemaining
    );

    Optional<PostingOrder> findTopByOwner_IdAndStatusAndRemainingUsesGreaterThanOrderByPaidAtDesc(
            Long ownerId,
            PaymentStatus status,
            int minRemaining
    );

    List<PostingOrder> findTop5ByStatusOrderByPaidAtDesc(PaymentStatus status);

    List<PostingOrder> findTop5ByStatusAndPaidAtLessThanEqualOrderByPaidAtDesc(PaymentStatus status, LocalDateTime date);

    @Query("SELECT SUM(po.amount) FROM PostingOrder po WHERE po.status = :status")
    Double calculateTotalRevenueByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT SUM(po.amount) FROM PostingOrder po WHERE po.status = :status AND po.paidAt BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueByStatusAndDateRange(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT po.owner.id) FROM PostingOrder po WHERE po.status = :status")
    long countDistinctOwnersPurchasedPackagesByStatus(@Param("status") PaymentStatus status);
}