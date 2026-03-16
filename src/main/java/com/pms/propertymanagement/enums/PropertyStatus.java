package com.pms.propertymanagement.enums;

/**
 * Property Status enum - Replaces boolean planSlot with extensible enum
 * Controls property access based on management plan limits
 */
public enum PropertyStatus {
    ACTIVE,
    PUBLISHED,
    DRAFT,
    OCCUPIED,
    PLAN_LOCKED,
    TEMPORARILY_LOCKED,
    ARCHIVED,
    ADMIN_LOCKED,
    SUSPENDED
}
