package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import com.pms.propertymanagement.service.FreeTrialService;
import com.pms.propertymanagement.service.ManagementPlanService;
import com.pms.propertymanagement.service.SubscriptionPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreeTrialServiceImpl implements FreeTrialService {

    private final SubscriptionRepository subscriptionRepository;
    private final ManagementPlanService managementPlanService;
    private final SubscriptionPolicyService subscriptionPolicyService;

    @Override
    public boolean hasEverHadSubscription(Long userId) {
        return subscriptionRepository.existsByUser_Id(userId);
    }

    @Override
    @Transactional
    public void createFreeTrialIfEligible(User user) {
        try {
            if (hasEverHadSubscription(user.getId())) {
                log.debug("User {} already has subscription history, skipping free trial", user.getId());
                return;
            }

            ManagementPlan freePlan = managementPlanService.getByCode("FREE");
            if (freePlan == null) {
                log.error("FREE management plan not found, cannot create free trial");
                return;
            }

            Subscription freeSubscription = new Subscription();
            freeSubscription.setUser(user);
            freeSubscription.setManagementPlanId(freePlan.getId());
            freeSubscription.setType(SubscriptionType.MANAGEMENT);
            freeSubscription.setStatus(SubscriptionStatus.ACTIVE);
            freeSubscription.setStartedAt(LocalDateTime.now());
            freeSubscription.setExpiredAt(LocalDateTime.now().plusDays(FREE_MANAGEMENT_PLAN_DAYS));

            freeSubscription.validateReferences();
            subscriptionPolicyService.validateSubscriptionReferences(freeSubscription);

            subscriptionRepository.save(freeSubscription);

            log.info("Created FREE management plan for user {} (expires in {} days, first post will have {} days)",
                    user.getId(), FREE_MANAGEMENT_PLAN_DAYS, FREE_POST_DURATION_DAYS);

        } catch (Exception e) {
            log.error("Error creating free trial for user {}", user.getId(), e);
        }
    }
}
