package com.pms.propertymanagement.enums;

public enum TransactionType {
    DEPOSIT("Nạp tiền", "text-green-600", "fa-plus"),
    PURCHASE("Mua gói đăng tin", "text-blue-600", "fa-shopping-cart"),
    EXTENSION("Gia hạn bài đăng", "text-orange-600", "fa-clock"),
    REFUND("Hoàn tiền", "text-purple-600", "fa-undo"),
    ADMIN_ADJUSTMENT("Điều chỉnh", "text-gray-600", "fa-cog");
    
    private final String displayName;
    private final String colorClass;
    private final String iconClass;
    
    TransactionType(String displayName, String colorClass, String iconClass) {
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
            case DEPOSIT -> "bg-green-100 text-green-800";
            case PURCHASE -> "bg-blue-100 text-blue-800";
            case EXTENSION -> "bg-orange-100 text-orange-800";
            case REFUND -> "bg-purple-100 text-purple-800";
            case ADMIN_ADJUSTMENT -> "bg-gray-100 text-gray-800";
        };
    }
}