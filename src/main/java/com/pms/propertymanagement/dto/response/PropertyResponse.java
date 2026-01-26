package com.pms.propertymanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyResponse {

    private Long id;
    private String name;
    private String addressNumber;
    private String formattedCreatedAt;
    private String categoryName;
}
