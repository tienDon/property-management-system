package com.pms.propertymanagement.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class RoomRequest {
    private String name;
    private Double price;
    private Double deposit;
    private Double area;
    private Integer maxOccupancy;
    private Integer bedCount;
    private Integer paymentCycle;
    private Boolean isElectricityWaterIncluded;
    private Long propertyId;
    private List<Long> serviceIds;
    private String description;
}
