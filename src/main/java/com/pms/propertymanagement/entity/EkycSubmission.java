package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ekyc_submissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EkycSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "front_hash", nullable = false)
    private String frontHash;

    @Column(name = "back_hash", nullable = false)
    private String backHash;

    @Column(name = "face_hash", nullable = false)
    private String faceHash;

    @Column(name = "front_cloudinary_url")
    private String frontCloudinaryUrl;

    @Column(name = "back_cloudinary_url")
    private String backCloudinaryUrl;

    @Column(name = "face_cloudinary_url")
    private String faceCloudinaryUrl;

    @Column(name = "front_cloudinary_public_id")
    private String frontCloudinaryPublicId;

    @Column(name = "back_cloudinary_public_id")
    private String backCloudinaryPublicId;

    @Column(name = "face_cloudinary_public_id")
    private String faceCloudinaryPublicId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

