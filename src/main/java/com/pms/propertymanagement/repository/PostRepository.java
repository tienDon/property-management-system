package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Post;
import com.pms.propertymanagement.enums.PostStatus;
import com.pms.propertymanagement.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Post entity with marketplace-optimized queries
 * Uses composite indexes for performance
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // === BASIC QUERIES ===

    /**
     * Find post by property ID (1:1 relationship)
     */
    Optional<Post> findByPropertyId(Long propertyId);

    /**
     * Check if post exists for property
     */
    boolean existsByPropertyId(Long propertyId);

    /**
     * Find posts by property owner
     */
    List<Post> findByPropertyOwnerId(Long ownerId);

    // === SLUG QUERIES (NEW ARCHITECTURE - for SEO-friendly URLs) ===

    /**
     * Find post by unique slug
     * Uses unique index: idx_post_slug_unique
     */
    Optional<Post> findBySlug(String slug);

    /**
     * Find visible marketplace post by slug (for public detail page)
     * OPTIMIZED: Checks status for marketplace visibility
     */
    @Query("SELECT p FROM Post p WHERE p.slug = :slug AND p.status = 'ACTIVE' " +
           "AND p.postExpiredAt > :currentTime")
    Optional<Post> findMarketplacePostBySlug(@Param("slug") String slug, 
                                            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Check if slug already exists (for duplicate validation)
     */
    boolean existsBySlug(String slug);

    /**
     * Check if slug exists excluding specific post ID (for edit validation)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p " +
           "WHERE p.slug = :slug AND p.id != :postId")
    boolean existsBySlugExcludingId(@Param("slug") String slug, @Param("postId") Long postId);

    // === MARKETPLACE QUERIES (using composite indexes) ===

    /**
     * Find active posts that haven't expired (marketplace listing)
     * Uses composite index: idx_marketplace_active (status, postExpiredAt)
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.postExpiredAt > :currentTime")
    List<Post> findActiveNonExpiredPosts(@Param("status") PostStatus status, 
                                        @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find boosted posts for marketplace priority
     * Uses composite index: idx_marketplace_boost (status, postExpiredAt, boostExpiredAt)
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status " +
           "AND p.postExpiredAt > :currentTime " +
           "AND p.boostExpiredAt > :currentTime " +
           "ORDER BY p.boostExpiredAt DESC")
    List<Post> findBoostedPosts(@Param("status") PostStatus status,
                               @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find all marketplace-visible posts (active + non-expired)
     * OPTIMIZED: Uses idx_marketplace_boost_query for best performance
     * Query pattern matches index: (status, postExpiredAt DESC, boostExpiredAt DESC)
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.postExpiredAt > :currentTime " +
           "ORDER BY " +
           "CASE WHEN p.boostExpiredAt IS NOT NULL AND p.boostExpiredAt > :currentTime THEN 0 ELSE 1 END, " +
           "p.boostExpiredAt DESC NULLS LAST, " +
           "p.postExpiredAt DESC")
    List<Post> findMarketplacePosts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find posts by specific property category for marketplace
     * OPTIMIZED: Query designed for idx_marketplace_boost_query performance  
     */
    @Query("SELECT p FROM Post p JOIN p.property prop WHERE p.status = 'ACTIVE' " +
           "AND p.postExpiredAt > :currentTime AND prop.category.id = :categoryId " +
           "ORDER BY " +
           "CASE WHEN p.boostExpiredAt IS NOT NULL AND p.boostExpiredAt > :currentTime THEN 0 ELSE 1 END, " +
           "p.boostExpiredAt DESC NULLS LAST, " +
           "p.postExpiredAt DESC")
    List<Post> findMarketplacePostsByCategory(@Param("categoryId") Long categoryId,
                                             @Param("currentTime") LocalDateTime currentTime);

    // === OWNER MANAGEMENT QUERIES ===

    /**
     * Find posts by owner with all statuses (for owner dashboard)
     */
    @Query("SELECT p FROM Post p WHERE p.property.owner.id = :ownerId " +
           "ORDER BY p.status, p.createdAt DESC")
    List<Post> findByOwnerOrderByStatus(@Param("ownerId") Long ownerId);

    /**
     * Find expired posts for cleanup/notification
     * OPTIMIZED: Uses idx_post_expiration_job (postExpiredAt, status)
     */
    @Query("SELECT p FROM Post p WHERE p.postExpiredAt < :currentTime AND p.status = 'ACTIVE'")
    List<Post> findExpiredActivePosts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find posts expiring within specified days (for renewal notifications)
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' " +
           "AND p.postExpiredAt BETWEEN :currentTime AND :futureTime")
    List<Post> findPostsExpiringWithin(@Param("currentTime") LocalDateTime currentTime,
                                      @Param("futureTime") LocalDateTime futureTime);

    // === BULK OPERATIONS ===

    /**
     * Extend postExpiredAt for ACTIVE posts owned by this user where current expiry is shorter
     * than newExpiredAt. Used when owner upgrades to a plan with longer postDurationDays.
     * Only extends (never shortens) posts.
     */
    @Modifying
    @Query("UPDATE Post p SET p.postExpiredAt = :newExpiredAt " +
           "WHERE p.property.owner.id = :ownerId AND p.status = 'ACTIVE' " +
           "AND p.postExpiredAt > CURRENT_TIMESTAMP " +
           "AND p.postExpiredAt < :newExpiredAt")
    int extendActivePostsExpiry(@Param("ownerId") Long ownerId,
                                @Param("newExpiredAt") LocalDateTime newExpiredAt);

    /**
     * Reactivate EXPIRED posts for a user when owner upgrades plan.
     * Sets status back to ACTIVE and extends postExpiredAt = now + newPlan.postDurationDays.
     */
    @Modifying
    @Query("UPDATE Post p SET p.status = 'ACTIVE', p.postExpiredAt = :newExpiredAt " +
           "WHERE p.property.owner.id = :ownerId AND p.status = 'EXPIRED'")
    int reactivateExpiredPosts(@Param("ownerId") Long ownerId,
                               @Param("newExpiredAt") LocalDateTime newExpiredAt);

    /**
     * Hide all posts for a user (when properties are locked)
     */
    @Modifying
    @Query("UPDATE Post p SET p.status = 'HIDDEN' WHERE p.property.owner.id = :ownerId " +
           "AND p.status = 'ACTIVE'")
    int hidePostsByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * Show all posts for a user (when properties are unlocked)
     */
    @Modifying
    @Query("UPDATE Post p SET p.status = 'ACTIVE' WHERE p.property.owner.id = :ownerId " +
           "AND p.status = 'HIDDEN' AND p.postExpiredAt > :currentTime")
    int showNonExpiredPostsByOwnerId(@Param("ownerId") Long ownerId, 
                                    @Param("currentTime") LocalDateTime currentTime);

    // === ANALYTICS QUERIES ===

    /**
     * Count active posts by owner
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.property.owner.id = :ownerId " +
           "AND p.status = 'ACTIVE' AND p.postExpiredAt > :currentTime")
    int countActivePostsByOwner(@Param("ownerId") Long ownerId, 
                               @Param("currentTime") LocalDateTime currentTime);

    /**
     * Count boosted posts by owner
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.property.owner.id = :ownerId " +
           "AND p.status = 'ACTIVE' AND p.postExpiredAt > :currentTime " +
           "AND p.boostExpiredAt > :currentTime")
    int countBoostedPostsByOwner(@Param("ownerId") Long ownerId,
                                @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find posts by property status (for property management integration)
     */
    @Query("SELECT p FROM Post p WHERE p.property.status = :propertyStatus")
    List<Post> findByPropertyStatus(@Param("propertyStatus") PropertyStatus propertyStatus);

    /**
     * Find all posts with a specific PostStatus
     * Used for staff moderation: findByStatus(PostStatus.PENDING_APPROVAL)
     */
    List<Post> findByStatus(PostStatus status);

    /**
     * Find posts by status ordered by creation date (oldest first for fairness)
     */
    List<Post> findByStatusOrderByCreatedAtAsc(PostStatus status);
}