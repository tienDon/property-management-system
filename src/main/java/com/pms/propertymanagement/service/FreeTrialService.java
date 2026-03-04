package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.User;

public interface FreeTrialService {

    int FREE_POST_DURATION_DAYS = 15;
    int FREE_MANAGEMENT_PLAN_DAYS = 15;

    boolean hasEverHadSubscription(Long userId);

    void createFreeTrialIfEligible(User user);
}
