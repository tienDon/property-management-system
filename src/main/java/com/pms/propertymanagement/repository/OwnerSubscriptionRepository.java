package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.OwnerSubscription;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerSubscriptionRepository extends JpaRepository<OwnerSubscription, Long> {
    
    Optional<OwnerSubscription> findByOwnerId(Long ownerId);
    
    @Query("SELECT os FROM OwnerSubscription os WHERE os.ownerId = :ownerId AND os.status = 'ACTIVE'")
    Optional<OwnerSubscription> findActiveByOwnerId(@Param("ownerId") Long ownerId);
    
    List<OwnerSubscription> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    
    @Query("SELECT os FROM OwnerSubscription os WHERE os.nextBillingDate <= :date AND os.status = 'ACTIVE'")
    List<OwnerSubscription> findDueForBilling(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(os) FROM OwnerSubscription os WHERE os.managementPlan.code = :planCode AND os.status = 'ACTIVE'")
    Long countActiveSubscriptionsByPlan(@Param("planCode") String planCode);
    
    boolean existsByOwnerIdAndStatus(Long ownerId, SubscriptionStatus status);
}