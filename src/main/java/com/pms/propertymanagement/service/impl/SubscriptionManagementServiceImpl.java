package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.enums.TransactionType;
import com.pms.propertymanagement.repository.PostRepository;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.ManagementPlanService;
import com.pms.propertymanagement.service.PostingPackageService;
import com.pms.propertymanagement.service.PropertyPolicyService;
import com.pms.propertymanagement.service.SubscriptionManagementService;
import com.pms.propertymanagement.service.SubscriptionPolicyService;
import com.pms.propertymanagement.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionManagementServiceImpl implements SubscriptionManagementService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPolicyService subscriptionPolicyService;
    private final PropertyPolicyService propertyPolicyService;
    private final ManagementPlanService managementPlanService;
    private final WalletService walletService;
    private final PostingPackageService postingPackageService;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Subscription createManagementSubscription(Long userId, Long managementPlanId, int durationDays) {
        log.info("Creating management subscription for user {}, plan {}, duration {} days",
                userId, managementPlanId, durationDays);

        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!subscriptionPolicyService.canCreateManagementSubscription(userId)) {
            throw new IllegalStateException("User already has an active management subscription");
        }

        ManagementPlan plan = managementPlanService.getById(managementPlanId);

        if (plan.getMonthlyPrice() > 0) {
            BigDecimal price = BigDecimal.valueOf(plan.getMonthlyPrice());
            if (!walletService.hasEnoughBalance(user, price)) {
                throw new IllegalStateException(
                        "Số dư ví không đủ. Cần " + String.format("%,.0f", (double) plan.getMonthlyPrice())
                        + " VNĐ để đăng ký gói " + plan.getName() + ".");
            }
        }

        subscriptionPolicyService.cancelActiveManagementSubscription(userId);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setManagementPlanId(managementPlanId);
        subscription.setType(SubscriptionType.MANAGEMENT);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusDays(durationDays));

        subscription.validateReferences();
        subscriptionPolicyService.validateSubscriptionReferences(subscription);

        subscription = subscriptionRepository.save(subscription);

        if (plan.getMonthlyPrice() > 0) {
            walletService.deduct(user, BigDecimal.valueOf(plan.getMonthlyPrice()),
                    TransactionType.PURCHASE,
                    "Đăng ký gói quản lý: " + plan.getName(),
                    "MGMT_PLAN_" + managementPlanId);
        }

        try {
            propertyPolicyService.unlockEligibleProperties(userId, plan);
            log.info("Successfully created management subscription {} for user {}", subscription.getId(), userId);
        } catch (Exception e) {
            log.error("Error applying property rules after subscription creation", e);
            throw e;
        }

        return subscription;
    }

    @Override
    @Transactional
    public Subscription createPostSubscription(Long userId, Long postPackageId) {
        log.info("Creating post subscription for user {}, package {}", userId, postPackageId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        var postPackage = postingPackageService.getById(postPackageId);
        if (postPackage == null) {
            throw new IllegalArgumentException("PostingPackage not found: " + postPackageId);
        }

        BigDecimal packagePrice = BigDecimal.valueOf(postPackage.getPrice());
        if (!walletService.hasEnoughBalance(user, packagePrice)) {
            throw new IllegalStateException("Số dư ví không đủ. Cần " + packagePrice + " VND để mua gói này.");
        }

        walletService.deduct(user, packagePrice,
                TransactionType.PURCHASE,
                "Mua gói đăng tin: " + postPackage.getName(),
                "POST_PKG_" + postPackageId);

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPostPackageId(postPackageId);
        subscription.setType(SubscriptionType.POST);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiredAt(LocalDateTime.now().plusDays(postPackage.getUsageLimit()));

        subscription.validateReferences();
        subscriptionPolicyService.validateSubscriptionReferences(subscription);

        subscription = subscriptionRepository.save(subscription);
        log.info("Successfully created post subscription {} for user {} (deducted {} VND)",
                subscription.getId(), userId, packagePrice);

        return subscription;
    }

    @Override
    @Transactional
    public Subscription upgradeManagementPlan(Long userId, Long newManagementPlanId) {
        log.info("Switching management plan for user {} to plan {}", userId, newManagementPlanId);

        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ManagementPlan newPlan = managementPlanService.getById(newManagementPlanId);

        if (newPlan.getMonthlyPrice() > 0) {
            BigDecimal price = BigDecimal.valueOf(newPlan.getMonthlyPrice());
            if (!walletService.hasEnoughBalance(user, price)) {
                throw new IllegalStateException(
                        "Số dư ví không đủ. Cần " + String.format("%,.0f", (double) newPlan.getMonthlyPrice())
                        + " VNĐ để đăng ký gói " + newPlan.getName() + ".");
            }
        }

        var currentSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        if (currentSub.isPresent()) {
            log.info("Cancelling current management subscription {} before switching plan", currentSub.get().getId());
            subscriptionPolicyService.cancelActiveManagementSubscription(userId);
        }

        Subscription newSub = createManagementSubscription(userId, newManagementPlanId, 30);

        // Extend/reactivate posts on upgrade:
        // - ACTIVE posts with remaining time < postDurationDays → extend expiry (never shorten)
        // - EXPIRED posts → reactivate (set ACTIVE) and give full postDurationDays from now
        if (newPlan.getPostDurationDays() > 0) {
            LocalDateTime newPostExpiry = LocalDateTime.now().plusDays(newPlan.getPostDurationDays());
            int updatedSubs = subscriptionRepository.extendActivePostSubscriptions(userId, newPostExpiry);
            int extendedPosts = postRepository.extendActivePostsExpiry(userId, newPostExpiry);
            int reactivatedPosts = postRepository.reactivateExpiredPosts(userId, newPostExpiry);
            log.info("Upgrade to plan {} for user {}: extended {} POST subscriptions, extended {} active posts, reactivated {} expired posts (expiry: {}, {} days)",
                    newPlan.getName(), userId, updatedSubs, extendedPosts, reactivatedPosts, newPostExpiry, newPlan.getPostDurationDays());
        }

        return newSub;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Subscription getActiveManagementSubscription(Long userId) {
        return subscriptionPolicyService.getActiveManagementSubscription(userId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPolicyService.SubscriptionCounts getSubscriptionCounts(Long userId) {
        return subscriptionPolicyService.getSubscriptionCounts(userId);
    }

    @Override
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
