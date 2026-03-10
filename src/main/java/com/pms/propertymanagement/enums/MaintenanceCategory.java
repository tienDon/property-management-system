package com.pms.propertymanagement.enums;

public enum MaintenanceCategory {
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    AIRCON("Aircon"),
    FURNITURE("Furniture"),
    OTHER("Other");

    private final String displayName;

    MaintenanceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

