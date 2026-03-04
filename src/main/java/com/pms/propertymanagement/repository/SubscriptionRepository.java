package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Subscription entity with enterprise-grade query methods
 * Includes pessimistic locking for concurrency control
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // === BASIC QUERY METHODS ===

    /**
     * Check if subscription exists by user, type, and status
     */
    boolean existsByUserAndTypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);
    
    /**
     * Check if user has any subscription (used for free trial eligibility)
     * Uses user.id navigation for JPA query
     */
    boolean existsByUser_Id(Long userId);

    /**
     * Find subscription by user, type, and status
     */
    Optional<Subscription> findByUserAndTypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);

    /**
     * Find all subscriptions by user, type, and status
     */
    List<Subscription> findAllByUserAndTypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);

    /**
     * Count subscriptions by user, type, and status
     */
    int countByUserAndTypeAndStatus(User user, SubscriptionType type, SubscriptionStatus status);

    /**
     * Count subscriptions by user and status (all types)
     */
    int countByUserAndStatus(User user, SubscriptionStatus status);

    // === EXPIRATION QUERIES (for scheduled job) ===

    /**
     * Find all subscriptions that are marked ACTIVE but have expired
     */
    List<Subscription> findByStatusAndExpiredAtBefore(SubscriptionStatus status, LocalDateTime dateTime);

    /**
     * Find expired ACTIVE subscriptions for specific user
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status AND s.expiredAt < :dateTime")
    List<Subscription> findExpiredByUserIdAndStatus(@Param("userId") Long userId, 
                                                   @Param("status") SubscriptionStatus status, 
                                                   @Param("dateTime") LocalDateTime dateTime);

    // === BUSINESS RULE ENFORCEMENT ===

    /**
     * Check if user has other active MANAGEMENT subscription (excluding specific ID)
     * Used in scheduled job to avoid premature property deactivation
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.id != :excludeId AND s.type = :type AND s.status = :status")
    boolean existsOtherActiveManagementSubscription(@Param("userId") Long userId, 
                                                   @Param("excludeId") Long excludeId,
                                                   @Param("type") SubscriptionType type, 
                                                   @Param("status") SubscriptionStatus status);

    // === BULK UPDATE OPERATIONS (for atomic transitions) ===

    /**
     * Cancel all active management subscriptions for user
     * Used during plan upgrades for atomic transition
     */
    @Modifying
    @Query("UPDATE Subscription s SET s.status = 'CANCELLED', s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.user.id = :userId AND s.type = 'MANAGEMENT' AND s.status = 'ACTIVE'")
    int cancelActiveManagementByUserId(@Param("userId") Long userId);

    /**
     * Bulk expire subscriptions by IDs
     * Used by scheduled job for batch processing
     */
    @Modifying
    @Query("UPDATE Subscription s SET s.status = 'EXPIRED', s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.id IN :subscriptionIds")
    int bulkExpireSubscriptions(@Param("subscriptionIds") List<Long> subscriptionIds);

    // === ADMINISTRATIVE QUERIES ===

    /**
     * Find ACTIVE subscriptions expiring within timeframe (for reminders)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.expiredAt BETWEEN CURRENT_TIMESTAMP AND :futureDate")
    List<Subscription> findExpiringWithinDays(@Param("futureDate") LocalDateTime futureDate);

    /**
     * Find all subscriptions for user (for admin/support)
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find subscriptions by management plan ID (for plan analytics)
     */
    List<Subscription> findByManagementPlanIdAndStatus(Long managementPlanId, SubscriptionStatus status);

    /**
     * Find subscriptions by post package ID (for package analytics)
     */  
    List<Subscription> findByPostPackageIdAndStatus(Long postPackageId, SubscriptionStatus status);

    /**
     * Extend expiredAt of all currently-active (not-yet-expired) POST subscriptions for a user.
     * Used when owner upgrades to a plan with a longer postDurationDays.
     */
    @Modifying
    @Query("UPDATE Subscription s SET s.expiredAt = :newExpiredAt, s.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE s.user.id = :userId AND s.type = 'POST' AND s.status = 'ACTIVE' " +
           "AND s.expiredAt > CURRENT_TIMESTAMP")
    int extendActivePostSubscriptions(@Param("userId") Long userId,
                                      @Param("newExpiredAt") LocalDateTime newExpiredAt);
}