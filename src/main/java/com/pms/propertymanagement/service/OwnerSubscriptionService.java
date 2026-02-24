package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.OwnerSubscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.ManagementPlan;

import java.util.Optional;

public interface OwnerSubscriptionService {

    OwnerSubscription getOrCreateSubscription(User owner);
    
    OwnerSubscription findByOwner(User owner);
    
    Optional<OwnerSubscription> findByOwnerId(Long ownerId);
    
    boolean canCreateProperty(User owner);
    
    void upgradeSubscription(User owner, ManagementPlan newPlan);
    
    void renewSubscription(User owner);
    
    int getAvailablePropertySlots(User owner);
    
    int getUsedPropertySlots(User owner);
}