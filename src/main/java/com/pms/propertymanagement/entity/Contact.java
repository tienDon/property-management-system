package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column( columnDefinition = "NVARCHAR(255)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(nullable = false)
    private Boolean isChecked = false;

    public Contact(String name, String phone, String note, User owner, Property property) {
        this.name = name;
        this.phone = phone;
        this.note = note;
        this.owner = owner;
        this.property = property;
        this.isChecked = false;
    }
}
