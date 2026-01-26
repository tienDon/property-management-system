package com.pms.propertymanagement.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchResponse {

    private Long id;
    private String title;
    private Double price;
    private Double area;
    private Integer maxPeople;

    private String categoryName;

    private String provinceName;
    private String districtName;
    private String wardName;

    private String addressNumber;
    private String primaryImageUrl;
}
