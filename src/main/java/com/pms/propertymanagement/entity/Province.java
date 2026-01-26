package com.pms.propertymanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "locations_provinces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Province {

    @Id
    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "province")
    private List<District> districts = new ArrayList<>();

    public Province(String code, String name) {
        this.code = code;
        this.name = name;
    }
}