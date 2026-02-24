package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * New architecture service to replace old PostingOrderService functionality
 * Handles property creation permissions and post management under new system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewPropertyManagementService {

    private final SubscriptionPolicyService subscriptionPolicyService;
    private final PropertyPolicyService propertyPolicyService;
    private final PostRepository postRepository;
    private final ManagementPlanService managementPlanService;

    /**
     * Check if user can create new property
     * Replaces old PostingOrderService.canPost() logic
     */
    public boolean canCreateProperty(Long userId) {
        // Get active management subscription
        Optional<Subscription> managementSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        
        if (managementSub.isEmpty()) {
            log.debug("User {} has no active management subscription, cannot create property", userId);
            return false;
        }

        // Get management plan
        ManagementPlan plan = managementPlanService.getById(managementSub.get().getManagementPlanId());
        
        // Check if under property limit
        boolean canCreate = propertyPolicyService.canCreateNewProperty(userId, plan);
        
        log.debug("User {} can create property: {}", userId, canCreate);
        return canCreate;
    }

    /**
     * Handle property creation process
     * Replaces old PostingOrderService.consumeOneUseForNewProperty() logic
     * 
     * Rule: 1 property = 1 post (maximum)
     * NEW ARCHITECTURE: Marketing fields passed as parameters, NOT from Property
     */
    @Transactional
    public void handleNewPropertyCreation(Property property, String postTitle, String postSlug, String postDescription) {
        Long userId = property.getOwner().getId();
        
        // Property is already created, now handle post creation
        createPostForProperty(property, postTitle, postSlug, postDescription);
        
        log.info("Completed property creation process for property {} (owner: {})", 
                property.getId(), userId);
    }

    /**
     * Create post for new property
     * Internal method for post creation
     * 
     * NEW ARCHITECTURE: Marketing fields (title, slug, description) passed as parameters
     * Property entity contains ONLY real estate data, Post contains ONLY marketing content
     */
    private Post createPostForProperty(Property property, String postTitle, String postSlug, String postDescription) {
        Long userId = property.getOwner().getId();
        
        // Check if property already has a post
        if (postRepository.existsByPropertyId(property.getId())) {
            log.warn("Property {} already has a post, skipping creation", property.getId());
            return postRepository.findByPropertyId(property.getId()).orElse(null);
        }

        // Create default post with marketing content from request parameters
        Post post = new Post();
        post.setProperty(property);
        
        // Set marketing fields from parameters (NEW CLEAN ARCHITECTURE)
        post.setTitle(postTitle);
        post.setSlug(postSlug);
        post.setDescription(postDescription);
        post.setViewCount(0);  // Initialize view counter
        
        // Determine post duration based on management plan
        int postDurationDays = determineInitialPostDuration(userId);
        post.setPostExpiredAt(LocalDateTime.now().plusDays(postDurationDays));
        
        // Post is ACTIVE by default with free trial
        post.setStatus(PostStatus.ACTIVE);
        
        post = postRepository.save(post);
        
        log.info("Created ACTIVE post {} with {}-day duration for property {} (owner: {})", 
                post.getId(), postDurationDays, property.getId(), userId);
        return post;
    }
    
    /**
     * Determine initial post duration based on user's management plan
     * FREE plan gets 15 days, others get 7 days
     */
    private int determineInitialPostDuration(Long userId) {
        Optional<Subscription> managementSub = subscriptionPolicyService.getActiveManagementSubscription(userId);
        
        if (managementSub.isEmpty()) {
            return 7; // Default fallback
        }
        
        ManagementPlan plan = managementPlanService.getById(managementSub.get().getManagementPlanId());
        
        // FREE plan gets 15 days for first property
        if ("FREE".equals(plan.getCode())) {
            return FreeTrialService.FREE_POST_DURATION_DAYS; // 15 days
        }
        
        // Other plans get 7 days default (they should buy post packages for longer)
        return 7;
    }

    /**
     * Get property creation status for user dashboard
     */
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
            ? (maxProperties == -1 ? "Unlimited properties allowed" : 
               String.format("You can create %d more properties", maxProperties - counts.active()))
            : "You have reached your property limit";
            
        return new PropertyCreationStatus(canCreate, message, counts.active(), maxProperties);
    }

    /**
     * Check if user can post property to marketplace (has active post subscription)
     */
    public boolean canPostToMarketplace(Long userId, Long propertyId) {
        // Check if property has active post
        Optional<Post> post = postRepository.findByPropertyId(propertyId);
        
        if (post.isEmpty()) {
            return false;
        }

        return post.get().isVisibleInMarketplace();
    }

    // DTO for property creation status
    public record PropertyCreationStatus(
        boolean canCreate,
        String message,
        int currentActiveProperties,
        int maxAllowedProperties  // -1 for unlimited
    ) {}
}