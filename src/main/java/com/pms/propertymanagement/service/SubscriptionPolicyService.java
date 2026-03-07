package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPolicyService {

    record SubscriptionCounts(int activeManagement, int activePosts, int expired, int cancelled) {}

    boolean canCreateManagementSubscription(Long userId);

    boolean canCreatePostSubscription(Long userId);

    Optional<Subscription> getActiveManagementSubscription(Long userId);

    List<Subscription> getActivePostSubscriptions(Long userId);

    void cancelActiveManagementSubscription(Long userId);

    boolean hasOtherActiveManagementSubscription(Long userId, Long excludeSubscriptionId);

    void validateSubscriptionReferences(Subscription subscription);

    List<Subscription> findExpiredActiveSubscriptions();

    List<Subscription> findExpiringWithinDays(int days);

    SubscriptionCounts getSubscriptionCounts(Long userId);

    boolean isSubscriptionExpired(Subscription subscription);
}
