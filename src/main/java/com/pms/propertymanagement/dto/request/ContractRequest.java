package com.pms.propertymanagement.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContractRequest {
    private Long propertyId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double rentPrice;
    private Double deposit;
    private Integer paymentCycle;
    private Boolean isElectricityWaterIncluded;
    private Integer bedCount;
    private String note;
    private List<Long> tenantIds; // IDs of selected tenants
}
