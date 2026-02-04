package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.enums.ServiceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceItemRequest {
    private String name;
    private Double price;
    private String unit;
    private ServiceType type; // FIXED hoặc METERED
    private Long propertyId;
}
