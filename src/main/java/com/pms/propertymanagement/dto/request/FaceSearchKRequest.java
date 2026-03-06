package com.pms.propertymanagement.dto.request;

import lombok.Data;

@Data
public class FaceSearchKRequest {
    private String img;
    private String unit;
    private Integer k;
    private Float threshold;
}
