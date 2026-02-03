package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PostingOrder;
import com.pms.propertymanagement.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
}