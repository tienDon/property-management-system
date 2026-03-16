package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "landlord_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandlordRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false, unique = true)
    private User landlord;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Double hygieneAverage = 0.0;

    @Column(nullable = false)
    private Double attitudeAverage = 0.0;

    @Column(nullable = false)
    private Double utilitiesAverage = 0.0;

    @Column(nullable = false)
    private Double safetyAverage = 0.0;

    @Column(nullable = false)
    private Double priceAverage = 0.0;

    @Column(nullable = false)
    private Long totalReviews = 0L;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
