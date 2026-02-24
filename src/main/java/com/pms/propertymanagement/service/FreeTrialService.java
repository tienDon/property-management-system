package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import com.pms.propertymanagement.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service to handle FREE trial subscription for new users
 * Automatically creates FREE management plan subscription (15 days)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FreeTrialService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final ManagementPlanService managementPlanService;
    private final SubscriptionPolicyService subscriptionPolicyService;
    
    private static final int FREE_MANAGEMENT_PLAN_DAYS = 30; // Management plan duration
    public static final int FREE_POST_DURATION_DAYS = 15; // Post duration for FREE plan
    
    /**
     * Check if user has ever had any subscription (FREE or paid)
     */
    public boolean hasEverHadSubscription(Long userId) {
        return subscriptionRepository.existsByUser_Id(userId);
    }
    
    /**
     * Create FREE trial subscription for new user
     * Only creates if user has NEVER had any subscription before
     */
    @Transactional
    public void createFreeTrialIfEligible(User user) {
        try {
            // Check if user already has any subscription (including expired ones)
            if (hasEverHadSubscription(user.getId())) {
                log.debug("User {} already has subscription history, skipping free trial", user.getId());
                return;
            }
            
            // Get FREE management plan
            ManagementPlan freePlan = managementPlanService.getByCode("FREE");
            if (freePlan == null) {
                log.error("FREE management plan not found, cannot create free trial");
                return;
            }
            
            // Create FREE subscription
            Subscription freeSubscription = new Subscription();
            freeSubscription.setUser(user);
            freeSubscription.setManagementPlanId(freePlan.getId());
            freeSubscription.setType(SubscriptionType.MANAGEMENT);
            freeSubscription.setStatus(SubscriptionStatus.ACTIVE);
            freeSubscription.setStartedAt(LocalDateTime.now());
            freeSubscription.setExpiredAt(LocalDateTime.now().plusDays(FREE_MANAGEMENT_PLAN_DAYS));
            
            // Validate and save
            freeSubscription.validateReferences();
            subscriptionPolicyService.validateSubscriptionReferences(freeSubscription);
            
            subscriptionRepository.save(freeSubscription);
            
            log.info("Created FREE management plan for user {} (expires in {} days, first post will have {} days)", 
                user.getId(), FREE_MANAGEMENT_PLAN_DAYS, FREE_POST_DURATION_DAYS);
            
        } catch (Exception e) {
            log.error("Error creating free trial for user {}", user.getId(), e);
            // Don't throw - this is a bonus feature, shouldn't block user login
        }
    }
}
