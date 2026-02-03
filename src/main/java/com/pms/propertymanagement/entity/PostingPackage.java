package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "posting_packages")
@Getter
@Setter
public class PostingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // POST_NEW

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(columnDefinition = "nvarchar(1000)")
    private String description;

    @Column(nullable = false)
    private int price; // VND

    @Column(nullable = false)
    private int usageLimit; // 1

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}