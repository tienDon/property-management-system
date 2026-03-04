package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;

import java.util.List;

public interface PropertyPolicyService {

    record PropertyCounts(int active, int planLocked, int adminLocked, int suspended) {}

    void applyPlanLimits(Long userId, ManagementPlan plan);

    void lockSelectedProperties(Long userId, List<Long> propertyIdsToLock);

    void unlockSelectedProperties(Long userId, List<Long> propertyIdsToUnlock);

    void unlockEligibleProperties(Long userId, ManagementPlan newPlan);

    void lockAllActiveProperties(Long userId);

    void unlockAllPlanLockedProperties(Long userId);

    boolean canCreateNewProperty(Long userId, ManagementPlan currentPlan);

    boolean canCreateProperty(Long userId);

    PropertyCounts getPropertyCounts(Long userId);
}
