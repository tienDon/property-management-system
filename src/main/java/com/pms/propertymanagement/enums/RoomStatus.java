package com.pms.propertymanagement.enums;

public enum RoomStatus {
    AVAILABLE("Còn trống"),
    RENTED("Đã thuê"),
    MAINTENANCE("Đang bảo trì");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
