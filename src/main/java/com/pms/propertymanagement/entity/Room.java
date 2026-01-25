package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "room_number")
    private String roomNumber;//so phong
    private Double price;// gia thue phong
    private Double area;// dien tich
    private String status = "AVAILABLE"; // AVAILABLE, RENTED, MAINTENANCE
    @Column(name = "max_people")
    private Integer maxPeople = 1; // Số người tối đa

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    @OneToMany(mappedBy = "room")
    private List<RoomImage> images = new ArrayList<>();
}
