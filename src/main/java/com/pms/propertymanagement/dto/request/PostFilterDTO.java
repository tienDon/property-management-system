package com.pms.propertymanagement.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class PostFilterDTO {
    private String keyword;
    private Long categoryId;
    private String provinceCode;
    private String wardCode;
    private Integer priceMin;
    private Integer priceMax;
    private Double areaMin;
    private Double areaMax;
    private Integer roomMin;
    private Integer roomMax;
    private List<Long> amenityIds;
}
