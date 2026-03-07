package com.pms.propertymanagement.enums;

/**
 * Subscription Type enum for the new architecture
 * MANAGEMENT - Controls property limits and features
 * POST - Controls marketplace visibility duration
 */
public enum SubscriptionType {
    MANAGEMENT,  // Management plan subscription (property limits + features)
    POST         // Post duration subscription (marketplace visibility)
    // NOTE: BOOST is NOT a subscription type - handled directly in Post entity
}