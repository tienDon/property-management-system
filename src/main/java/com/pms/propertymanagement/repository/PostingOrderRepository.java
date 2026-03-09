package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
    @Query("SELECT SUM(po.amount) FROM PostingOrder po WHERE po.status = :status")
    Double calculateTotalRevenueByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COUNT(DISTINCT po.owner.id) FROM PostingOrder po WHERE po.status = :status")
    long countDistinctOwnersPurchasedPackagesByStatus(@Param("status") PaymentStatus status);
}