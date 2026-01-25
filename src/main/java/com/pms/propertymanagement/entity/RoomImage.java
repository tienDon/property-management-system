package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
}
