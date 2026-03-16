package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.repository.PostRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initializer to create Posts for existing Properties
 * NEW ARCHITECTURE: 1 Property = 1 Post
 * Marketing content generated from Property.name
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostInitializer {

    private final PropertyRepository propertyRepository;
    private final PostRepository postRepository;

    @Value("${pms.demo.refresh-expired-posts:true}")
    private boolean refreshExpiredPosts;

    @Transactional
    public void init() {
        LocalDateTime now = LocalDateTime.now();

        // Only run if there are properties without posts
        if (propertyRepository.count() == 0) {
            log.info("No properties found, skipping post initialization");
            return;
        }

        long existingPosts = postRepository.count();
        if (existingPosts > 0) {
            if (refreshExpiredPosts) {
                int visibleCount = postRepository.findMarketplacePosts(now).size();
                if (visibleCount == 0) {
                    LocalDateTime newExpiry = now.plusDays(30);
                    int refreshed = postRepository.refreshExpiredMarketplacePosts(now, newExpiry);
                    log.info("Marketplace had no visible posts; refreshed {} expired posts (expiry: {})", refreshed, newExpiry);
                }
            }
            log.info("{} posts already exist, skipping initialization", existingPosts);
            return;
        }

        List<Property> properties = propertyRepository.findAll();
        int createdCount = 0;

        for (Property property : properties) {
            // Check if post already exists for this property
            if (postRepository.existsByPropertyId(property.getId())) {
                log.debug("Post already exists for property {}", property.getId());
                continue;
            }

            try {
                // Create post with marketing content generated from Property name
                Post post = new Post();
                post.setProperty(property);
                
                // NEW ARCHITECTURE: Generate marketing fields from Property.name
                // Property entity no longer contains title/slug/description
                String propertyName = property.getName();
                post.setTitle(propertyName);
                post.setSlug(SlugUtil.makeSlug(propertyName + "-" + property.getId()));
                post.setDescription("Chi tiết về " + propertyName + ". " +
                    "Diện tích: " + property.getAcreage() + "m². " +
                    "Giá: " + String.format("%,d", property.getPrice()) + "đ/tháng. " +
                    "Liên hệ để xem phòng và tìm hiểu thêm.");
                post.setViewCount(0);
                
                // Set 7-day free trial for existing properties
                post.setPostExpiredAt(now.plusDays(365));
                post.setStatus(PostStatus.ACTIVE);
                
                postRepository.save(post);
                createdCount++;
                
                log.debug("Created post for property {} (slug: {})", property.getId(), post.getSlug());
                
            } catch (Exception e) {
                log.error("Failed to create post for property {}: {}", property.getId(), e.getMessage());
            }
        }

        log.info("Post initialization completed: {} posts created for {} properties", 
                createdCount, properties.size());
    }
}
