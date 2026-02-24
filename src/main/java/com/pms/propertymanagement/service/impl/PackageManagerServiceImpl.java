package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.ManagementPlanDTO;
import com.pms.propertymanagement.dto.OwnerSubscriptionDTO;
import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.dto.plan.PackageSummaryDTO;
import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.PropertyPost;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.mapper.ManagementPlanMapper;
import com.pms.propertymanagement.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageManagerServiceImpl implements PackageManagerService {

    private final ManagementPlanService managementPlanService;
    private final OwnerSubscriptionService ownerSubscriptionService;
    private final PostPackageService postPackageService;
    private final PropertyPostService propertyPostService;
    private final PropertyService propertyService;
    private final ManagementPlanMapper managementPlanMapper;

    @Override
    public List<ManagementPlanDTO> getAvailableManagementPlans() {
        List<ManagementPlan> plans = managementPlanService.findAllActive();
        return managementPlanMapper.toDTOList(plans);
    }

    @Override
    public OwnerSubscriptionDTO getOwnerSubscription(User owner) {
        var subscription = ownerSubscriptionService.getOrCreateSubscription(owner);
        
        OwnerSubscriptionDTO dto = new OwnerSubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setOwnerId(owner.getId());
        dto.setOwnerName(owner.getFullName());
        dto.setManagementPlan(managementPlanMapper.toDTO(subscription.getManagementPlan()));
        dto.setSubscriptionDate(subscription.getStartDate());
        dto.setNextBillingDate(subscription.getNextBillingDate());
        dto.setStatus(subscription.getStatus());
        dto.setUsedPropertySlots(ownerSubscriptionService.getUsedPropertySlots(owner));
        dto.setAvailablePropertySlots(ownerSubscriptionService.getAvailablePropertySlots(owner));
        dto.setCanCreateProperty(ownerSubscriptionService.canCreateProperty(owner));
        
        return dto;
    }

    @Override
    @Transactional
    public OwnerSubscriptionDTO upgradeManagementPlan(User owner, String planCode) {
        ManagementPlan newPlan = managementPlanService.getByCode(planCode);
        ownerSubscriptionService.upgradeSubscription(owner, newPlan);
        return getOwnerSubscription(owner);
    }

    @Override
    public boolean canCreateProperty(User owner) {
        return ownerSubscriptionService.canCreateProperty(owner);
    }

    @Override
    public int getAvailablePropertySlots(User owner) {
        return ownerSubscriptionService.getAvailablePropertySlots(owner);
    }

    @Override
    public List<PostPackageDTO> getAvailablePostPackages() {
        return postPackageService.getAllPackages();
    }

    @Override
    @Transactional
    public void createPropertyPost(Property property, String postPackageCode) {
        PostingPackage postPackage = postPackageService.getByCode(postPackageCode);
        propertyPostService.createPost(property, postPackage);
    }

    @Override
    @Transactional
    public void renewPropertyPost(Property property, String postPackageCode) {
        PropertyPost currentPost = propertyPostService.findByProperty(property);
        PostingPackage newPackage = postPackageService.getByCode(postPackageCode);
        propertyPostService.renewPost(currentPost, newPackage);
    }

    @Override
    public boolean hasActivePost(Property property) {
        try {
            PropertyPost post = propertyPostService.findByProperty(property);
            return post.isActive();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getPropertyPostStatus(Property property) {
        try {
            PropertyPost post = propertyPostService.findByProperty(property);
            int daysLeft = (int) post.getDaysLeft();
            
            if (daysLeft < 0) {
                return "Đã hết hạn";
            } else if (daysLeft == 0) {
                return "Hết hạn hôm nay";
            } else if (daysLeft <= 3) {
                return String.format("Còn %d ngày (sắp hết hạn)", daysLeft);
            } else {
                return String.format("Còn %d ngày", daysLeft);
            }
        } catch (IllegalArgumentException e) {
            return "Chưa đăng tin";
        }
    }

    @Override
    @Transactional
    public void createPropertyWithPost(Property property, String postPackageCode, User owner) {
        // 1. Kiểm tra subscription
        if (!canCreateProperty(owner)) {
            throw new IllegalStateException("Bạn đã hết slot tạo property. Hãy nâng cấp gói quản lý.");
        }
        
        // 2. Property sẽ được tạo bởi PropertyService
        // 3. Tạo post
        createPropertyPost(property, postPackageCode);
    }

    @Override
    public PackageSummaryDTO getOwnerPackageSummary(User owner) {
        PackageSummaryDTO summary = new PackageSummaryDTO();
        summary.subscription = getOwnerSubscription(owner);
        
        List<PropertyPost> activePosts = propertyPostService.findActivePostsByOwner(owner);
        summary.activePosts = activePosts.stream()
                .map(this::toPropertyPostSummaryDTO)
                .collect(Collectors.toList());
        
        List<PropertyPost> expiringPosts = propertyPostService.findExpiringPosts(7); // 7 days ahead
        summary.expiringPosts = expiringPosts.stream()
                .filter(post -> post.getOwnerId().equals(owner.getId()))
                .map(this::toPropertyPostSummaryDTO)
                .collect(Collectors.toList());
        
        return summary;
    }

    // Helper methods - toPostPackageDTO removed, using PostPackageService.getAllPackages() instead

    private PackageSummaryDTO.PropertyPostSummaryDTO toPropertyPostSummaryDTO(PropertyPost post) {
        PackageSummaryDTO.PropertyPostSummaryDTO dto = new PackageSummaryDTO.PropertyPostSummaryDTO();
        dto.propertyId = post.getPropertyId();
        
        // Get property information using PropertyService
        dto.propertyName = "Property #" + post.getPropertyId();
        
        dto.postStatus = post.getStatus().toString();
        dto.postPackageName = post.getPostPackage().getName();
        dto.daysLeft = (int) post.getDaysLeft();
        dto.canRenew = post.isEligibleForRenewal();
        
        return dto;
    }
}