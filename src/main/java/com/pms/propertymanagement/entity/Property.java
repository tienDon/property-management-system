package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "address_number")
    private  String addressNumber;
    @Column(name = "ward_code")
    private String wardCode;
    @ManyToOne
    @JoinColumn(name ="owner_id")
    private  User owner;
    private String description;

}
