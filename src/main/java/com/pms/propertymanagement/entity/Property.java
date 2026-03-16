package com.pms.propertymanagement.entity;

import com.pms.propertymanagement.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@Setter
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Internal management name (not for public display)
     * For public display, use Post.title
     */
    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(nullable = false)
    private int numberOfRooms;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private double acreage;

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String addressNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    //Vĩ độ
    private Double latitude;

    //Kinh độ
    private Double longitude;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyRule> rules = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code")
    private Ward ward; // Phường/Xã

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner; // Chủ nhà

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceItem> serviceItems = new ArrayList<>();

    //Tiện nghi
    @ManyToMany
    @JoinTable(
            name = "property_amenity",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    //Môi trường
    @ManyToMany
    @JoinTable(
            name = "property_surrounding",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "surrounding_id")
    )
    private Set<Surrounding> surroundings = new HashSet<>();

    //Đối tượng
    @ManyToMany
    @JoinTable(
            name = "property_target",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "target_id")
    )
    private Set<TargetTenant>  targetTenants = new HashSet<>();

    // === NEW ARCHITECTURE: Property Status Management ===
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) DEFAULT 'DRAFT'")
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Column(name = "management_locked_at")
    private LocalDateTime managementLockedAt;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // === Property Status Management Methods ===
    public void lockByPlan() {
        this.status = PropertyStatus.TEMPORARILY_LOCKED;
        this.managementLockedAt = LocalDateTime.now();
    }

    public void unlockByPlan() {
        if (this.status == PropertyStatus.TEMPORARILY_LOCKED || this.status == PropertyStatus.PLAN_LOCKED) {
            this.status = PropertyStatus.PUBLISHED;
            this.managementLockedAt = null;
        }
    }

    public boolean isActive() {
        return status == PropertyStatus.PUBLISHED || status == PropertyStatus.ACTIVE;
    }

    public boolean isPlanLocked() {
        return status == PropertyStatus.TEMPORARILY_LOCKED || status == PropertyStatus.PLAN_LOCKED;
    }

    public boolean isManageable() {
        return status == PropertyStatus.PUBLISHED || status == PropertyStatus.ACTIVE || status == PropertyStatus.DRAFT;
    }

}
