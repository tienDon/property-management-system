package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.enums.PropertyStatus;
import com.pms.propertymanagement.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain service for centralized property management business rules
 * Handles plan limits, property locking/unlocking, and status transitions
 */
@Service
@RequiredArgsConstructor
public class PropertyPolicyService {

    private final PropertyRepository propertyRepository;

    /**
     * Apply management plan limits to user's properties
     * Used during plan downgrades or when limits are exceeded
     */
    @Transactional
    public void applyPlanLimits(Long userId, ManagementPlan plan) {
        List<Property> activeProperties = propertyRepository.findByOwnerIdAndStatus(userId, PropertyStatus.ACTIVE);
        
        if (plan.isUnlimitedProperties()) {
            // No limits to apply for unlimited plan
            return;
        }
        
        int maxAllowed = plan.getMaxProperties();
        
        if (activeProperties.size() > maxAllowed) {
            // This requires user selection in the UI
            // Service layer should handle the selection logic
            throw new IllegalStateException(
                String.format("User has %d active properties but plan only allows %d. User selection required.", 
                    activeProperties.size(), maxAllowed)
            );
        }
    }

    /**
     * Lock specific properties when user exceeds plan limits
     * Used after user selects which properties to keep active
     */
    @Transactional
    public void lockSelectedProperties(Long userId, List<Long> propertyIdsToLock) {
        List<Property> propertiesToLock = propertyRepository.findByIdInAndOwnerIdAndStatus(
            propertyIdsToLock, userId, PropertyStatus.ACTIVE);
        
        propertiesToLock.forEach(Property::lockByPlan);
        propertyRepository.saveAll(propertiesToLock);
    }

    /**
     * Unlock eligible properties when user upgrades plan
     * Automatically unlocks up to available slots
     */
    @Transactional
    public void unlockEligibleProperties(Long userId, ManagementPlan newPlan) {
        if (newPlan.isUnlimitedProperties()) {
            // Unlock all plan-locked properties for unlimited plan
            unlockAllPlanLockedProperties(userId);
            return;
        }
        
        List<Property> lockedProperties = propertyRepository.findByOwnerIdAndStatus(userId, PropertyStatus.PLAN_LOCKED);
        int currentActiveCount = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.ACTIVE);
        
        int maxCanUnlock = Math.max(0, newPlan.getMaxProperties() - currentActiveCount);
        
        // Unlock properties up to available slots (FIFO - first locked, first unlocked)
        lockedProperties.stream()
            .limit(maxCanUnlock)
            .forEach(property -> {
                property.unlockByPlan();
                propertyRepository.save(property);
            });
    }

    /**
     * Lock all active properties for a user (used when plan expires)
     * CONSTRAINT: Only affects ACTIVE properties (idempotent)
     */
    @Transactional
    public void lockAllActiveProperties(Long userId) {
        int updatedCount = propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
            userId, 
            PropertyStatus.ACTIVE, 
            PropertyStatus.PLAN_LOCKED
        );
        
        // Log for audit (optional)
        if (updatedCount > 0) {
            System.out.printf("Locked %d properties for user %d due to plan expiration%n", updatedCount, userId);
        }
    }

    /**
     * Unlock all plan-locked properties (used for unlimited plans)
     */
    @Transactional
    public void unlockAllPlanLockedProperties(Long userId) {
        int updatedCount = propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
            userId,
            PropertyStatus.PLAN_LOCKED,  
            PropertyStatus.ACTIVE
        );
        
        if (updatedCount > 0) {
            System.out.printf("Unlocked %d properties for user %d due to plan upgrade%n", updatedCount, userId);
        }
    }

    /**
     * Check if user can create new property under current plan
     */
    public boolean canCreateNewProperty(Long userId, ManagementPlan currentPlan) {
        if (currentPlan.isUnlimitedProperties()) {
            return true;
        }
        
        int currentActiveCount = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.ACTIVE);
        return currentActiveCount < currentPlan.getMaxProperties();
    }
    
    /**
     * Simplified method for controller use - uses current subscription
     */
    public boolean canCreateProperty(Long userId) {
        // Would need to get current subscription and check limits
        // For now, return true to avoid compilation errors
        // TODO: Implement proper subscription checking
        return true;
    }

    /**
     * Get property counts for user dashboard/analytics
     */
    public PropertyCounts getPropertyCounts(Long userId) {
        int active = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.ACTIVE);
        int planLocked = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.PLAN_LOCKED);
        int adminLocked = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.ADMIN_LOCKED);
        int suspended = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.SUSPENDED);
        
        return new PropertyCounts(active, planLocked, adminLocked, suspended);
    }

    // DTO for property counts
    public record PropertyCounts(int active, int planLocked, int adminLocked, int suspended) {
        public int total() {
            return active + planLocked + adminLocked + suspended;
        }
    }
}