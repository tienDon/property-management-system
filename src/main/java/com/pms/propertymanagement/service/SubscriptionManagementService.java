package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Subscription;

import java.util.List;

public interface SubscriptionManagementService {

    Subscription createManagementSubscription(Long userId, Long managementPlanId, int durationDays);

    Subscription createPostSubscription(Long userId, Long postPackageId);

    Subscription upgradeManagementPlan(Long userId, Long newManagementPlanId);

    List<Subscription> getUserSubscriptions(Long userId);

    Subscription getActiveManagementSubscription(Long userId);

    SubscriptionPolicyService.SubscriptionCounts getSubscriptionCounts(Long userId);

    void cancelSubscription(Long subscriptionId, Long userId);
}
