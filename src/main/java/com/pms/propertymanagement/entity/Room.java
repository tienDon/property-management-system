package com.pms.propertymanagement.entity;

import com.pms.propertymanagement.constants.RoomStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    private String roomNumber;
    private Double price;
    private Double area;
    private Double depositAmount;

    private Integer maxPeople;

    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.AVAILABLE;

    private String genderType = "ALL"; // MALE, FEMALE, ALL

    private Boolean isDeleted = false;
}
