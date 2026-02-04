package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "property_images")
@Getter
@Setter
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // Đường dẫn ảnh (có thể là link Firebase, Cloudinary hoặc local)

    @Column(name = "is_primary")
    private Boolean isPrimary = false; // Ảnh này có phải là ảnh bìa (Cover) của bài đăng không?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

}
