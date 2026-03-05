package com.pms.propertymanagement.enums;

public enum TransactionStatus {
    PENDING("Đang xử lý", "text-yellow-600", "fa-clock"),
    COMPLETED("Hoàn thành", "text-green-600", "fa-check"),
    FAILED("Thất bại", "text-red-600", "fa-times"),
    CANCELLED("Đã hủy", "text-gray-600", "fa-ban");
    
    private final String displayName;
    private final String colorClass;
    private final String iconClass;
    
    TransactionStatus(String displayName, String colorClass, String iconClass) {
        this.displayName = displayName;
        this.colorClass = colorClass;
        this.iconClass = iconClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorClass() {
        return colorClass;
    }
    
    public String getIconClass() {
        return iconClass;
    }
    
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "bg-yellow-100 text-yellow-800";
            case COMPLETED -> "bg-green-100 text-green-800";
            case FAILED -> "bg-red-100 text-red-800";
            case CANCELLED -> "bg-gray-100 text-gray-800";
        };
    }
}