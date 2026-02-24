package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.enums.TransactionType;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Core subscription management service
 * Implements atomic subscription creation with pessimistic locking
 * Enforces all business rules through policy services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionManagementService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPolicyService subscriptionPolicyService;
    private final PropertyPolicyService propertyPolicyService;
    private final ManagementPlanService managementPlanService;
    private final WalletService walletService;
    private final PostPackageService postPackageService;

    /**
     * Create MANAGEMENT subscription with atomic transition
     * CONCURRENCY CONTROL: Uses User-level pessimistic locking
     * BUSINESS RULE: Cancels existing active management subscription
     */
    @Transactional
    public Subscription createManagementSubscription(Long userId, Long managementPlanId, int durationDays) {
        log.info("Creating management subscription for user {}, plan {}, duration {} days", 
                userId, managementPlanId, durationDays);

        // STEP 1: Acquire pessimistic lock on User (aggregate root)
        User user = userRepository.findByIdWithLock(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // STEP 2: Validate business rules
        if (!subscriptionPolicyService.canCreateManagementSubscription(userId)) {
            throw new IllegalStateException("User already has an active management subscription");
        }

        ManagementPlan plan = managementPlanService.getById(managementPlanId);

        // STEP 2b: Check wallet for paid plans
        if (plan.getMonthlyPrice() > 0) {
            BigDecimal price = BigDecimal.valueOf(plan.getMonthlyPrice());
            if (!walletService.hasEnoughBalance(user, price)) {
                throw new IllegalStateException(
                    "Số dư ví không đủ. Cần " + String.format("%,.0f", (double) plan.getMonthlyPrice())
                    + " VNĐ để đăng ký gói " + plan.getName() + ".");
            }
        }

        // STEP 3: Cancel existing active MANAGEMENT subscription (atomic transition)
        subscriptionPolicyService.cancelActiveManagementSubscription(userId);

        // STEP 4: Create new subscription
        Subscription subscription = new Subscription();
        subscription.setUser(user);  // Use entity mapping, not raw ID
        subscription.setManagementPlanId(managementPlanId);
        subscription.setType(SubscriptionType.MANAGEMENT);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusDays(durationDays));

        // STEP 5: ENFORCE Validation - Critical!
        subscription.validateReferences();
        subscriptionPolicyService.validateSubscriptionReferences(subscription);

        // STEP 6: Save subscription
        subscription = subscriptionRepository.save(subscription);

        // STEP 6b: Deduct wallet for paid plans
        if (plan.getMonthlyPrice() > 0) {
            walletService.deduct(user, BigDecimal.valueOf(plan.getMonthlyPrice()),
                TransactionType.PURCHASE,
                "Đăng ký gói quản lý: " + plan.getName(),
                "MGMT_PLAN_" + managementPlanId);
        }

        // STEP 7: Apply property management rules
        try {
            propertyPolicyService.unlockEligibleProperties(userId, plan);
            log.info("Successfully created management subscription {} for user {}", subscription.getId(), userId);
        } catch (Exception e) {
            log.error("Error applying property rules after subscription creation", e);
            throw e;
        }

        return subscription;
    }

    /**
     * Create POST subscription (no locking needed - multiple allowed)
     * NEW: Deducts wallet balance for package purchase
     */
    @Transactional
    public Subscription createPostSubscription(Long userId, Long postPackageId) {
        log.info("Creating post subscription for user {}, package {}", userId, postPackageId);

        // Validate and get user entity
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Get PostPackage to check price and duration
        var postPackage = postPackageService.getById(postPackageId);
        if (postPackage == null) {
            throw new IllegalArgumentException("PostPackage not found: " + postPackageId);
        }

        // Check wallet balance
        BigDecimal packagePrice = BigDecimal.valueOf(postPackage.getPrice());
        if (!walletService.hasEnoughBalance(user, packagePrice)) {
            throw new IllegalStateException("Số dư ví không đủ. Cần " + packagePrice + " VND để mua gói này.");
        }

        // Deduct wallet balance
        walletService.deduct(user, packagePrice, 
            TransactionType.PURCHASE,
            "Mua gói đăng tin: " + postPackage.getName(),
            "POST_PKG_" + postPackageId);

        // POST subscriptions don't need business rule checking (multiple allowed)
        Subscription subscription = new Subscription();
        subscription.setUser(user);  // Use entity mapping
        subscription.setPostPackageId(postPackageId);
        subscription.setType(SubscriptionType.POST);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        
        // Get duration from PostPackage (assuming usageLimit represents days)
        subscription.setExpiredAt(LocalDateTime.now().plusDays(postPackage.getUsageLimit()));

        // ENFORCE Validation - Critical!
        subscription.validateReferences();
        subscriptionPolicyService.validateSubscriptionReferences(subscription);

        subscription = subscriptionRepository.save(subscription);
        log.info("Successfully created post subscription {} for user {} (deducted {} VND)", 
            subscription.getId(), userId, packagePrice);

        return subscription;
    }

    /**
     * Switch management plan (upgrade or downgrade) with atomic transition.
     * Steps: check wallet → cancel existing → deduct wallet → create new subscription.
     */
    @Transactional
    public Subscription upgradeManagementPlan(Long userId, Long newManagementPlanId) {
        log.info("Switching management plan for user {} to plan {}", userId, newManagementPlanId);

        User user = userRepository.findByIdWithLock(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ManagementPlan newPlan = managementPlanService.getById(newManagementPlanId);

        // Check wallet balance for paid plans
        if (newPlan.getMonthlyPrice() > 0) {
            BigDecimal price = BigDecimal.valueOf(newPlan.getMonthlyPrice());
            if (!walletService.hasEnoughBalance(user, price)) {
                throw new IllegalStateException(
                    "Số dư ví không đủ. Cần " + String.format("%,.0f", (double) newPlan.getMonthlyPrice())
                    + " VNĐ để đăng ký gói " + newPlan.getName() + ".");
            }
        }

        // Cancel existing active sub BEFORE creating new one
        var currentSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        if (currentSub.isPresent()) {
            log.info("Cancelling current management subscription {} before switching plan", currentSub.get().getId());
            subscriptionPolicyService.cancelActiveManagementSubscription(userId);
        }

        // Deduct wallet for paid plans
        if (newPlan.getMonthlyPrice() > 0) {
            walletService.deduct(user, BigDecimal.valueOf(newPlan.getMonthlyPrice()),
                TransactionType.PURCHASE,
                "Đăng ký gói quản lý: " + newPlan.getName(),
                "MGMT_PLAN_" + newManagementPlanId);
        }

        // Create new subscription (canCreate check will now pass since we already cancelled)
        return createManagementSubscription(userId, newManagementPlanId, 30);
    }

    /**
     * Get all subscriptions for user (for dashboard)
     */
    @Transactional(readOnly = true)
    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get active management subscription for user
     */
    @Transactional(readOnly = true)
    public Subscription getActiveManagementSubscription(Long userId) {
        return subscriptionPolicyService.getActiveManagementSubscription(userId)
            .orElse(null);
    }

    /**
     * Get subscription counts for user dashboard
     */
    @Transactional(readOnly = true)
    public SubscriptionPolicyService.SubscriptionCounts getSubscriptionCounts(Long userId) {
        return subscriptionPolicyService.getSubscriptionCounts(userId);
    }

    /**
     * Cancel subscription (manual cancellation)
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId, Long userId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (!subscription.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Subscription does not belong to user");
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Can only cancel active subscriptions");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // If it's a management subscription, lock properties
        if (subscription.getType() == SubscriptionType.MANAGEMENT) {
            boolean hasOtherActive = subscriptionPolicyService.hasOtherActiveManagementSubscription(
                userId, subscriptionId);
                
            if (!hasOtherActive) {
                propertyPolicyService.lockAllActiveProperties(userId);
                log.info("Locked properties for user {} after management subscription cancellation", userId);
            }
        }

        log.info("Cancelled subscription {} for user {}", subscriptionId, userId);
    }
}