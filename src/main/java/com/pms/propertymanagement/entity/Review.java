package com.pms.propertymanagement.entity;

import com.pms.propertymanagement.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"contract_id", "tenant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    private User landlord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // 5 Categories
    @Column(nullable = false)
    private Integer hygieneRating; // Vệ sinh

    @Column(nullable = false)
    private Integer attitudeRating; // Thái độ

    @Column(nullable = false)
    private Integer utilitiesRating; // Tiện ích

    @Column(nullable = false)
    private Integer safetyRating; // An toàn

    @Column(nullable = false)
    private Integer priceRating; // Giá

    @Column(nullable = false)
    private Double averageRating; // Điểm trung bình

    @Column(columnDefinition = "nvarchar(MAX)")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "nvarchar(MAX)")
    private String rejectionReason;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL)
    private ReviewReply reply;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<ReviewVote> votes = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateAverageRating();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateAverageRating();
    }

    public void calculateAverageRating() {
        this.averageRating = (hygieneRating + attitudeRating + utilitiesRating + safetyRating + priceRating) / 5.0;
    }
}
