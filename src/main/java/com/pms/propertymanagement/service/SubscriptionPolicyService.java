package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain service for centralized subscription business rules
 * Enforces subscription constraints at service layer (not DB level)
 */
@Service
@RequiredArgsConstructor
public class SubscriptionPolicyService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Check if user can create a new MANAGEMENT subscription
     * BUSINESS RULE: Only one active MANAGEMENT subscription per user
     */
    public boolean canCreateManagementSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return !subscriptionRepository.existsByUserAndTypeAndStatus(
            user, 
            SubscriptionType.MANAGEMENT, 
            SubscriptionStatus.ACTIVE
        );
    }

    /**
     * Check if user can create POST subscription
     * POST subscriptions can have multiple active instances
     */
    public boolean canCreatePostSubscription(Long userId) {
        // No restrictions on POST subscriptions - users can have multiple
        return true;
    }

    /**
     * Get active MANAGEMENT subscription for user
     * Returns empty if no active management subscription exists
     */
    public Optional<Subscription> getActiveManagementSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return subscriptionRepository.findByUserAndTypeAndStatus(
            user, 
            SubscriptionType.MANAGEMENT, 
            SubscriptionStatus.ACTIVE
        );
    }

    /**
     * Get all active POST subscriptions for user
     */
    public List<Subscription> getActivePostSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return subscriptionRepository.findAllByUserAndTypeAndStatus(
            user, 
            SubscriptionType.POST, 
            SubscriptionStatus.ACTIVE
        );
    }

    /**
     * Cancel existing active MANAGEMENT subscription
     * Used when upgrading/downgrading management plans
     * CONSTRAINT: Atomic transition to prevent race conditions
     */
    @Transactional
    public void cancelActiveManagementSubscription(Long userId) {
        Optional<Subscription> existing = getActiveManagementSubscription(userId);
        
        if (existing.isPresent()) {
            Subscription subscription = existing.get();
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
        }
    }

    /**
     * Check if user has any other active MANAGEMENT subscription
     * Used in scheduled job to avoid deactivating properties prematurely
     * EXCLUDES the subscription being checked for expiration
     */
    public boolean hasOtherActiveManagementSubscription(Long userId, Long excludeSubscriptionId) {
        return subscriptionRepository.existsOtherActiveManagementSubscription(
            userId, 
            excludeSubscriptionId, 
            SubscriptionType.MANAGEMENT, 
            SubscriptionStatus.ACTIVE
        );
    }

    /**
     * Validate subscription references based on type
     * Ensures data integrity at service layer
     */
    public void validateSubscriptionReferences(Subscription subscription) {
        if (subscription.getType() == SubscriptionType.MANAGEMENT) {
            if (subscription.getManagementPlanId() == null) {
                throw new IllegalArgumentException("MANAGEMENT subscription must have managementPlanId");
            }
            if (subscription.getPostPackageId() != null) {
                throw new IllegalArgumentException("MANAGEMENT subscription cannot have postPackageId");
            }
        }
        
        if (subscription.getType() == SubscriptionType.POST) {
            if (subscription.getPostPackageId() == null) {
                throw new IllegalArgumentException("POST subscription must have postPackageId");
            }
            if (subscription.getManagementPlanId() != null) {
                throw new IllegalArgumentException("POST subscription cannot have managementPlanId");
            }
        }
    }

    /**
     * Find subscriptions that have expired but are still marked as ACTIVE
     * Used by scheduled job for automatic expiration processing
     */
    public List<Subscription> findExpiredActiveSubscriptions() {
        return subscriptionRepository.findByStatusAndExpiredAtBefore(
            SubscriptionStatus.ACTIVE, 
            LocalDateTime.now()
        );
    }
    
    /**
     * CRITICAL FIX: Find ACTIVE subscriptions expiring within specified days
     * Used by reminder notifications to find subscriptions expiring SOON
     * This is DIFFERENT from findExpiredActiveSubscriptions (which finds already expired)
     */
    public List<Subscription> findExpiringWithinDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureThreshold = now.plusDays(days);
        
        return subscriptionRepository.findExpiringWithinDays(futureThreshold);
    }

    /**
     * Get subscription counts for user dashboard/analytics
     */
    public SubscriptionCounts getSubscriptionCounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        int activeManagement = subscriptionRepository.countByUserAndTypeAndStatus(
            user, SubscriptionType.MANAGEMENT, SubscriptionStatus.ACTIVE);
        int activePosts = subscriptionRepository.countByUserAndTypeAndStatus(
            user, SubscriptionType.POST, SubscriptionStatus.ACTIVE);
        int expired = subscriptionRepository.countByUserAndStatus(
            user, SubscriptionStatus.EXPIRED);
        int cancelled = subscriptionRepository.countByUserAndStatus(
            user, SubscriptionStatus.CANCELLED);
        
        return new SubscriptionCounts(activeManagement, activePosts, expired, cancelled);
    }

    /**
     * Check if subscription is truly expired (considering both status and time)
     */
    public boolean isSubscriptionExpired(Subscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.EXPIRED || 
               subscription.getExpiredAt().isBefore(LocalDateTime.now());
    }

    // DTO for subscription counts
    public record SubscriptionCounts(int activeManagement, int activePosts, int expired, int cancelled) {
        public int totalActive() {
            return activeManagement + activePosts;
        }
        
        public int total() {
            return activeManagement + activePosts + expired + cancelled;
        }
    }
}