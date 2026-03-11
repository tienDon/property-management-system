package com.pms.propertymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityDTO {
    private String ownerName;
    private String packageName;
    private Double amount;
    private LocalDateTime date;
    private String status;
}
