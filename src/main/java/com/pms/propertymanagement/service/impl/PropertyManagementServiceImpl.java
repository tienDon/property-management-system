package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.repository.PostRepository;
import com.pms.propertymanagement.service.ManagementPlanService;
import com.pms.propertymanagement.service.PropertyManagementService;
import com.pms.propertymanagement.service.PropertyPolicyService;
import com.pms.propertymanagement.service.SubscriptionPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyManagementServiceImpl implements PropertyManagementService {

    private final SubscriptionPolicyService subscriptionPolicyService;
    private final PropertyPolicyService propertyPolicyService;
    private final PostRepository postRepository;
    private final ManagementPlanService managementPlanService;

    @Override
    public boolean canCreateProperty(Long userId) {
        Optional<Subscription> managementSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        if (managementSub.isEmpty()) {
            log.debug("User {} has no active management subscription, cannot create property", userId);
            return false;
        }
        ManagementPlan plan = managementPlanService.getById(managementSub.get().getManagementPlanId());
        boolean canCreate = propertyPolicyService.canCreateNewProperty(userId, plan);
        log.debug("User {} can create property: {}", userId, canCreate);
        return canCreate;
    }

    @Override
    @Transactional
    public void handleNewPropertyCreation(Property property, String postTitle, String postSlug, String postDescription) {
        createPostForProperty(property, postTitle, postSlug, postDescription);
        log.info("Completed property creation process for property {} (owner: {})",
                property.getId(), property.getOwner().getId());
    }

    private Post createPostForProperty(Property property, String postTitle, String postSlug, String postDescription) {
        Long userId = property.getOwner().getId();

        if (postRepository.existsByPropertyId(property.getId())) {
            log.warn("Property {} already has a post, skipping creation", property.getId());
            return postRepository.findByPropertyId(property.getId()).orElse(null);
        }

        Post post = new Post();
        post.setProperty(property);
        post.setTitle(postTitle);
        post.setSlug(postSlug);
        post.setDescription(postDescription);
        post.setViewCount(0);

        int postDurationDays = determineInitialPostDuration(userId);
        post.setPostExpiredAt(LocalDateTime.now().plusDays(postDurationDays));
        post.setStatus(PostStatus.ACTIVE);

        post = postRepository.save(post);
        log.info("Created ACTIVE post {} with {}-day duration for property {} (owner: {})",
                post.getId(), postDurationDays, property.getId(), userId);
        return post;
    }

    private int determineInitialPostDuration(Long userId) {
        Optional<Subscription> managementSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        if (managementSub.isEmpty()) return 7;
        ManagementPlan plan = managementPlanService.getById(managementSub.get().getManagementPlanId());
        return plan.getPostDurationDays();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyCreationStatus getPropertyCreationStatus(Long userId) {
        Optional<Subscription> managementSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        if (managementSub.isEmpty()) {
            return new PropertyCreationStatus(false, "No active management subscription", 0, 0);
        }
        ManagementPlan plan = managementPlanService.getById(managementSub.get().getManagementPlanId());
        PropertyPolicyService.PropertyCounts counts = propertyPolicyService.getPropertyCounts(userId);
        boolean canCreate = propertyPolicyService.canCreateNewProperty(userId, plan);
        int maxProperties = plan.isUnlimitedProperties() ? -1 : plan.getMaxProperties();
        String message = canCreate
                ? (maxProperties == -1 ? "Unlimited properties allowed"
                        : String.format("You can create %d more properties", maxProperties - counts.active()))
                : "You have reached your property limit";
        return new PropertyCreationStatus(canCreate, message, counts.active(), maxProperties);
    }

    @Override
    public boolean canPostToMarketplace(Long userId, Long propertyId) {
        Optional<Post> post = postRepository.findByPropertyId(propertyId);
        return post.isPresent() && post.get().isVisibleInMarketplace();
    }
}
