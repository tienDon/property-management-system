package com.pms.propertymanagement.dto.request;

import com.pms.propertymanagement.enums.MaintenanceCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceCreateForm {
    private MaintenanceCategory category;
    private String description;
}

