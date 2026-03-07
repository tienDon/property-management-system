package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.SubscriptionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPolicyServiceImpl implements SubscriptionPolicyService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public boolean canCreateManagementSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return !subscriptionRepository.existsByUserAndTypeAndStatus(
                user, SubscriptionType.MANAGEMENT, SubscriptionStatus.ACTIVE);
    }

    @Override
    public boolean canCreatePostSubscription(Long userId) {
        return true;
    }

    @Override
    public Optional<Subscription> getActiveManagementSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return subscriptionRepository.findByUserAndTypeAndStatus(
                user, SubscriptionType.MANAGEMENT, SubscriptionStatus.ACTIVE);
    }

    @Override
    public List<Subscription> getActivePostSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return subscriptionRepository.findAllByUserAndTypeAndStatus(
                user, SubscriptionType.POST, SubscriptionStatus.ACTIVE);
    }

    @Override
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

    @Override
    public boolean hasOtherActiveManagementSubscription(Long userId, Long excludeSubscriptionId) {
        return subscriptionRepository.existsOtherActiveManagementSubscription(
                userId, excludeSubscriptionId, SubscriptionType.MANAGEMENT, SubscriptionStatus.ACTIVE);
    }

    @Override
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

    @Override
    public List<Subscription> findExpiredActiveSubscriptions() {
        return subscriptionRepository.findByStatusAndExpiredAtBefore(
                SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }

    @Override
    public List<Subscription> findExpiringWithinDays(int days) {
        LocalDateTime futureThreshold = LocalDateTime.now().plusDays(days);
        return subscriptionRepository.findExpiringWithinDays(futureThreshold);
    }

    @Override
    public SubscriptionCounts getSubscriptionCounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        int activeManagement = subscriptionRepository.countByUserAndTypeAndStatus(
                user, SubscriptionType.MANAGEMENT, SubscriptionStatus.ACTIVE);
        int activePosts = subscriptionRepository.countByUserAndTypeAndStatus(
                user, SubscriptionType.POST, SubscriptionStatus.ACTIVE);
        int expired = subscriptionRepository.countByUserAndStatus(user, SubscriptionStatus.EXPIRED);
        int cancelled = subscriptionRepository.countByUserAndStatus(user, SubscriptionStatus.CANCELLED);
        return new SubscriptionCounts(activeManagement, activePosts, expired, cancelled);
    }

    @Override
    public boolean isSubscriptionExpired(Subscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.EXPIRED
                || subscription.getExpiredAt().isBefore(LocalDateTime.now());
    }
}
