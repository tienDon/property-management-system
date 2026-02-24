package com.pms.propertymanagement.enums;

public enum MaintenanceCategory {
    ELECTRICAL("Điện"),
    PLUMBING("Nước"),
    AIRCON("Điều hòa"),
    FURNITURE("Nội thất"),
    OTHER("Khác");

    private final String displayName;

    MaintenanceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

