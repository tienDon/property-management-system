package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations_districts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class District {

    @Id
    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @ManyToOne
    @JoinColumn(name = "province_code")
    private Province province;

    @OneToMany(mappedBy = "district")
    private List<Ward> wards = new ArrayList<>();

    public District(String code, String name, Province province) {
        this.code = code;
        this.name = name;
        this.province = province;
    }
}

