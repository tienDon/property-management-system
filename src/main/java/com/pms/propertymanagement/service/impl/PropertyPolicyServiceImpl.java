package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.enums.PropertyStatus;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.service.PropertyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyPolicyServiceImpl implements PropertyPolicyService {

    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public void applyPlanLimits(Long userId, ManagementPlan plan) {
        List<Property> activeProperties = propertyRepository.findByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));
        if (plan.isUnlimitedProperties()) return;
        int maxAllowed = plan.getMaxProperties();
        if (activeProperties.size() > maxAllowed) {
            throw new IllegalStateException(
                    String.format("User has %d active properties but plan only allows %d. User selection required.",
                            activeProperties.size(), maxAllowed));
        }
    }

    @Override
    @Transactional
    public void lockSelectedProperties(Long userId, List<Long> propertyIdsToLock) {
        List<Property> propertiesToLock = propertyRepository.findByIdInAndOwnerIdAndStatusIn(
                propertyIdsToLock, userId, List.of(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));
        propertiesToLock.forEach(Property::lockByPlan);
        propertyRepository.saveAll(propertiesToLock);
    }

    @Override
    @Transactional
    public void unlockSelectedProperties(Long userId, List<Long> propertyIdsToUnlock) {
        if (propertyIdsToUnlock == null || propertyIdsToUnlock.isEmpty()) return;
        List<Property> propertiesToUnlock = propertyRepository.findByIdInAndOwnerIdAndStatusIn(
                propertyIdsToUnlock, userId, List.of(PropertyStatus.TEMPORARILY_LOCKED, PropertyStatus.PLAN_LOCKED));
        propertiesToUnlock.forEach(Property::unlockByPlan);
        propertyRepository.saveAll(propertiesToUnlock);
    }

    @Override
    @Transactional
    public void unlockEligibleProperties(Long userId, ManagementPlan newPlan) {
        if (newPlan.isUnlimitedProperties()) {
            unlockAllPlanLockedProperties(userId);
            return;
        }
        List<Property> lockedProperties = propertyRepository.findByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.TEMPORARILY_LOCKED, PropertyStatus.PLAN_LOCKED));
        int currentActiveCount = propertyRepository.countByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));
        int maxCanUnlock = Math.max(0, newPlan.getMaxProperties() - currentActiveCount);
        lockedProperties.stream()
                .limit(maxCanUnlock)
                .forEach(property -> {
                    property.unlockByPlan();
                    propertyRepository.save(property);
                });
    }

    @Override
    @Transactional
    public void lockAllActiveProperties(Long userId) {
        int updatedCount = propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
                userId, PropertyStatus.PUBLISHED, PropertyStatus.TEMPORARILY_LOCKED);
        updatedCount += propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
                userId, PropertyStatus.ACTIVE, PropertyStatus.TEMPORARILY_LOCKED);
        if (updatedCount > 0) {
            System.out.printf("Locked %d properties for user %d due to plan expiration%n", updatedCount, userId);
        }
    }

    @Override
    @Transactional
    public void unlockAllPlanLockedProperties(Long userId) {
        int updatedCount = propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
                userId, PropertyStatus.TEMPORARILY_LOCKED, PropertyStatus.PUBLISHED);
        updatedCount += propertyRepository.updateStatusByOwnerIdAndCurrentStatus(
                userId, PropertyStatus.PLAN_LOCKED, PropertyStatus.PUBLISHED);
        if (updatedCount > 0) {
            System.out.printf("Unlocked %d properties for user %d due to plan upgrade%n", updatedCount, userId);
        }
    }

    @Override
    public boolean canCreateNewProperty(Long userId, ManagementPlan currentPlan) {
        if (currentPlan.isUnlimitedProperties()) return true;
        int currentActiveCount = propertyRepository.countByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));
        return currentActiveCount < currentPlan.getMaxProperties();
    }

    @Override
    public boolean canCreateProperty(Long userId) {
        return true;
    }

    @Override
    public PropertyCounts getPropertyCounts(Long userId) {
        int active = propertyRepository.countByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.PUBLISHED, PropertyStatus.ACTIVE));
        int planLocked = propertyRepository.countByOwnerIdAndStatusIn(
                userId, List.of(PropertyStatus.TEMPORARILY_LOCKED, PropertyStatus.PLAN_LOCKED));
        int adminLocked = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.ADMIN_LOCKED);
        int suspended = propertyRepository.countByOwnerIdAndStatus(userId, PropertyStatus.SUSPENDED);
        return new PropertyCounts(active, planLocked, adminLocked, suspended);
    }
}
