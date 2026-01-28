package com.pms.propertymanagement.enums;

public enum ContractStatus {
    ACTIVE("Đang hiệu lực"),
    EXPIRING_SOON("Sắp hết hạn"),
    EXPIRED("Đã hết hạn"),
    TERMINATED("Đã kết thúc");

    private final String displayName;

    ContractStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
