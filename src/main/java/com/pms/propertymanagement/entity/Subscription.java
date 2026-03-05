package com.pms.propertymanagement.entity;

import com.pms.propertymanagement.enums.SubscriptionStatus;
import com.pms.propertymanagement.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Universal Subscription entity - handles both MANAGEMENT and POST subscriptions
 * Business rules enforced at service layer, not DB constraints
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_user", columnList = "user_id"),
        @Index(name = "idx_subscriptions_type_status", columnList = "type, status"),
        @Index(name = "idx_subscriptions_expired_at", columnList = "expired_at")
})
@Getter
@Setter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User relationship - Proper FK mapping without cascade
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Convenience method for backward compatibility
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    // Separate clear foreign key references
    @Column(name = "management_plan_id")
    private Long managementPlanId;  // nullable, for type = MANAGEMENT

    @Column(name = "post_package_id")
    private Long postPackageId;     // nullable, for type = POST

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;  // MANAGEMENT | POST (no BOOST)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === Business Logic Validation ===
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == SubscriptionStatus.ACTIVE && !expiredAt.isBefore(now);
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }
    
    public boolean isExpiringSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureThreshold = now.plusDays(days);
        return status == SubscriptionStatus.ACTIVE && 
               expiredAt.isAfter(now) && 
               expiredAt.isBefore(futureThreshold);
    }

    public boolean isManagement() {
        return type == SubscriptionType.MANAGEMENT;
    }

    public boolean isPost() {
        return type == SubscriptionType.POST;
    }

    // === Validation Helper Methods ===
    public void validateReferences() {
        if (type == SubscriptionType.MANAGEMENT && managementPlanId == null) {
            throw new IllegalStateException("MANAGEMENT subscription must have managementPlanId");
        }
        if (type == SubscriptionType.POST && postPackageId == null) {
            throw new IllegalStateException("POST subscription must have postPackageId");
        }
    }
}