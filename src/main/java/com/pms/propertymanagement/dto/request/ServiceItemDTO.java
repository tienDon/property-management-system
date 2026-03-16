package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.enums.ServiceType;
import lombok.Data;

@Data
public class ServiceItemDTO {
    private String name;
    private Double price;
    private String unit;
    private ServiceType type; // FIXED or METERED
}
