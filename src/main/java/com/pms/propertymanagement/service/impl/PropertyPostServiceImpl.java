package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.PropertyPost;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.repository.PropertyPostRepository;
import com.pms.propertymanagement.service.PropertyPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropertyPostServiceImpl implements PropertyPostService {

    private final PropertyPostRepository propertyPostRepository;

    @Override
    @Transactional
    public PropertyPost createPost(Property property, PostingPackage postPackage) {
        // Check if property already has an active post
        Optional<PropertyPost> existing = propertyPostRepository.findByPropertyIdAndStatus(
                property.getId(), PostStatus.ACTIVE);
        
        if (existing.isPresent()) {
            throw new IllegalStateException("Property đã có post đang hoạt động. Hãy gia hạn hoặc tạo post mới sau khi hết hạn.");
        }

        PropertyPost post = new PropertyPost();
        post.setPropertyId(property.getId());
        post.setOwnerId(property.getOwner().getId());
        post.setPostPackage(postPackage);
        post.setStartDate(LocalDateTime.now());
        // NEW ARCHITECTURE: Calculate expiry based on package code
        int durationDays = calculateDuration(postPackage.getCode());
        post.setExpiryDate(LocalDateTime.now().plusDays(durationDays));
        post.setStatus(PostStatus.ACTIVE);
        
        return propertyPostRepository.save(post);
    }

    @Override
    @Transactional
    public PropertyPost renewPost(PropertyPost post, PostingPackage postPackage) {
        post.setPostPackage(postPackage);
        post.setStartDate(LocalDateTime.now());
        // NEW ARCHITECTURE: Calculate expiry based on package code
        int durationDays = calculateDuration(postPackage.getCode());
        post.setExpiryDate(LocalDateTime.now().plusDays(durationDays));
        post.setStatus(PostStatus.ACTIVE);
        
        return propertyPostRepository.save(post);
    }

    @Override
    @Transactional
    public void extendPost(PropertyPost post, int additionalDays) {
        post.setExpiryDate(post.getExpiryDate().plusDays(additionalDays));
        propertyPostRepository.save(post);
    }

    @Override
    public PropertyPost findByProperty(Property property) {
        return propertyPostRepository.findByPropertyId(property.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy post của property: " + property.getId()));
    }

    @Override
    public Optional<PropertyPost> findByPropertyId(Long propertyId) {
        return propertyPostRepository.findByPropertyId(propertyId);
    }

    @Override
    public List<PropertyPost> findActivePostsByOwner(User owner) {
        return propertyPostRepository.findActivePostsByOwnerId(owner.getId());
    }

    @Override
    public List<PropertyPost> findExpiringPosts(int daysAhead) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysAhead);
        return propertyPostRepository.findExpiringPosts(threshold);
    }

    @Override
    public List<PropertyPost> findExpiredPosts() {
        return propertyPostRepository.findExpiredPosts(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void expirePost(PropertyPost post) {
        post.setStatus(PostStatus.EXPIRED);
        propertyPostRepository.save(post);
    }

    @Override
    @Transactional
    public void incrementViewCount(PropertyPost post) {
        post.setTotalViews(post.getTotalViews() + 1);
        propertyPostRepository.save(post);
    }

    @Override
    @Transactional
    public void incrementContactCount(PropertyPost post) {
        post.setTotalContacts(post.getTotalContacts() + 1);
        propertyPostRepository.save(post);
    }
    
    // NEW ARCHITECTURE: Calculate duration based on package code
    private int calculateDuration(String code) {
        return switch (code) {
            case "POST_NEW" -> 15;       // 100k for 15 days
            case "POST_STANDARD" -> 30;  // 250k for 30 days
            case "POST_PREMIUM" -> 60;   // 500k for 60 days
            case "POST_ENTERPRISE" -> 90; // 1M for 90 days
            default -> 15;
        };
    }
}