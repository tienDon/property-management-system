package com.pms.propertymanagement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {

    private Long id;
    private String title;
    private int price;
    private String categoryName;
    private double acreage;
    private String wardName;
    private String provinceName;
    private String imageUrl;
    private String slug;
}
