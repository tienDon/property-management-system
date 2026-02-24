package com.pms.propertymanagement.enums;

public enum MaintenanceStatus {
    PENDING("Chờ xử lý"),
    ASSIGNED("Đã phân công"),
    IN_PROGRESS("Đang xử lý"),
    COMPLETED("Hoàn thành"),
    CONFIRMED("Đã xác nhận"),
    REJECTED("Từ chối"),
    REOPENED("Mở lại");

    private final String displayName;

    MaintenanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

