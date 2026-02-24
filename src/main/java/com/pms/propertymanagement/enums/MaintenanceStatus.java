package com.pms.propertymanagement.enums;

public enum MaintenanceStatus {
    PENDING("Pending"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CONFIRMED("Confirmed"),
    REJECTED("Rejected"),
    REOPENED("Reopened");

    private final String displayName;

    MaintenanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

