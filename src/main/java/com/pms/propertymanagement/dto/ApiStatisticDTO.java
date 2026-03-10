package com.pms.propertymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiStatisticDTO {
    private String apiName;
    private String path;
    private Long total;
    private Long success;
    private Long inputError;
    private Long clientError;
    private Long systemError;
}
