package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations_wards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Id
    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @ManyToOne
    @JoinColumn(name = "district_code")
    private District district;

    @OneToMany(mappedBy = "ward")
    private List<Property> properties = new ArrayList<>();

    public Ward(String code, String name, District district) {
        this.code = code;
        this.name = name;
        this.district = district;
    }
}

