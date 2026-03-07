package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PropertyPost;
import com.pms.propertymanagement.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Property Posts
 */
@Repository
public interface PropertyPostRepository extends JpaRepository<PropertyPost, Long> {
    
    Optional<PropertyPost> findByPropertyId(Long propertyId);
    
    Optional<PropertyPost> findByPropertyIdAndStatus(Long propertyId, PostStatus status);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.propertyId = :propertyId AND pp.status = 'ACTIVE'")
    Optional<PropertyPost> findActivePostByProperty(@Param("propertyId") Long propertyId);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.ownerId = :ownerId")
    List<PropertyPost> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.ownerId = :ownerId AND pp.status = 'ACTIVE'")
    List<PropertyPost> findActivePostsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.ownerId = :ownerId AND pp.status = 'ACTIVE'")
    List<PropertyPost> findActivePostsByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.status = 'ACTIVE' AND pp.expiryDate <= :date")
    List<PropertyPost> findExpiredPosts(@Param("date") LocalDateTime date);
    
    @Query("SELECT pp FROM PropertyPost pp WHERE pp.status = 'ACTIVE' AND pp.expiryDate <= :threshold")
    List<PropertyPost> findExpiringPosts(@Param("threshold") LocalDateTime threshold);
    
    @Query("""
        SELECT pp FROM PropertyPost pp 
        WHERE pp.status = 'ACTIVE' 
        AND pp.autoRenew = true 
        AND pp.expiryDate BETWEEN :startDate AND :endDate
    """)
    List<PropertyPost> findEligibleForRenewal(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(pp) FROM PropertyPost pp WHERE pp.ownerId = :ownerId AND pp.status = 'ACTIVE'")
    Integer countActivePostsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("""
        SELECT pp FROM PropertyPost pp 
        WHERE pp.ownerId = :ownerId 
        AND pp.status IN ('ACTIVE', 'EXPIRED') 
        ORDER BY pp.createdAt DESC
    """)
    List<PropertyPost> findPostHistoryByOwner(@Param("ownerId") Long ownerId);
    
    @Query("""
        SELECT pp FROM PropertyPost pp
        JOIN PropertySlot ps ON pp.propertyId = ps.propertyId
        WHERE ps.isActive = false AND pp.status = 'ACTIVE'
    """)
    List<PropertyPost> findActivePostsWithInactiveSlots();
    
    // Analytics queries
    @Query("""
        SELECT pp.postPackage.code, COUNT(pp), SUM(pp.postPackage.price)
        FROM PropertyPost pp 
        WHERE pp.createdAt BETWEEN :startDate AND :endDate
        GROUP BY pp.postPackage.code
    """)
    List<Object[]> getPackageUsageStats(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("""
        SELECT DATE(pp.createdAt), COUNT(pp), SUM(pp.postPackage.price)
        FROM PropertyPost pp 
        WHERE pp.createdAt BETWEEN :startDate AND :endDate
        GROUP BY DATE(pp.createdAt)
        ORDER BY DATE(pp.createdAt)
    """)
    List<Object[]> getDailyPostStats(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
}