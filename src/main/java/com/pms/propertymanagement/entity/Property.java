package com.pms.propertymanagement.entity;

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

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String name;

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String title;

    @Column(unique = true, nullable = false)
    private String slug; // Dùng để xem chi tiết bài đăng (SEO)

    @Column(nullable = false)
    private int numberOfRooms;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private double acreage;

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String addressNumber;

    @Column(columnDefinition = "nvarchar(255)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

//    //Vĩ độ
//    private Double latitude;
//
//    //Kinh độ
//    private Double longitude;



    @ManyToOne(fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    @JoinColumn(name = "ward_code")
    private Ward ward; // Phường/Xã

    @ManyToOne(fetch = FetchType.LAZY,   cascade = CascadeType.ALL)
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


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

}
