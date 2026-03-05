package com.pms.propertymanagement.enums;

/**
 * Property Status enum - Replaces boolean planSlot with extensible enum
 * Controls property access based on management plan limits
 */
public enum PropertyStatus {
    ACTIVE,         // Property is active and can be managed
    PLAN_LOCKED,    // Locked due to management plan limits (view-only)
    ADMIN_LOCKED,   // Locked by administrator
    SUSPENDED       // Suspended (future use for compliance issues)
}