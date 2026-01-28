package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.enums.ServiceType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemResponse {
    private Long id;
    private String name;
    private Double price;
    private String unit;
    private ServiceType type;
    private String propertyName; // Để hiển thị cột "Nhà trọ"
    private String formattedCreatedAt;
}
