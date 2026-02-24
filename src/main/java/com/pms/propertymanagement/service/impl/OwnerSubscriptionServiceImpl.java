package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.OwnerSubscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.OwnerSubscriptionRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.service.ManagementPlanService;
import com.pms.propertymanagement.service.OwnerSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerSubscriptionServiceImpl implements OwnerSubscriptionService {

    private final OwnerSubscriptionRepository ownerSubscriptionRepository;
    private final ManagementPlanService managementPlanService;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public OwnerSubscription getOrCreateSubscription(User owner) {
        return ownerSubscriptionRepository.findByOwnerId(owner.getId())
                .orElseGet(() -> createDefaultSubscription(owner));
    }

    @Override
    public OwnerSubscription findByOwner(User owner) {
        return ownerSubscriptionRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy subscription của owner: " + owner.getId()));
    }

    @Override
    public Optional<OwnerSubscription> findByOwnerId(Long ownerId) {
        return ownerSubscriptionRepository.findByOwnerId(ownerId);
    }

    @Override
    public boolean canCreateProperty(User owner) {
        OwnerSubscription subscription = getOrCreateSubscription(owner);
        return subscription.canCreateProperty();
    }

    @Override
    @Transactional
    public void upgradeSubscription(User owner, ManagementPlan newPlan) {
        OwnerSubscription subscription = getOrCreateSubscription(owner);
        subscription.setManagementPlan(newPlan);
        
        // Reset billing date to current time when upgrading
        subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));
        
        ownerSubscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void renewSubscription(User owner) {
        OwnerSubscription subscription = findByOwner(owner);
        subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));
        ownerSubscriptionRepository.save(subscription);
    }

    @Override
    public int getAvailablePropertySlots(User owner) {
        OwnerSubscription subscription = getOrCreateSubscription(owner);
        ManagementPlan plan = subscription.getManagementPlan();
        
        if (plan.getMaxProperties() == -1) {
            return Integer.MAX_VALUE; // Unlimited
        }
        
        int usedSlots = getUsedPropertySlots(owner);
        return Math.max(0, plan.getMaxProperties() - usedSlots);
    }

    @Override
    public int getUsedPropertySlots(User owner) {
        return Math.toIntExact(propertyRepository.countByOwnerId(owner.getId()));
    }

    private OwnerSubscription createDefaultSubscription(User owner) {
        ManagementPlan freePlan = managementPlanService.getDefaultPlan();
        
        OwnerSubscription subscription = new OwnerSubscription();
        subscription.setOwnerId(owner.getId());
        subscription.setManagementPlan(freePlan);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));
        
        return ownerSubscriptionRepository.save(subscription);
    }
}