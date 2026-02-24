package com.pms.propertymanagement.enums;

/**
 * Post Status enum - Production Ready
 * Controls post lifecycle and marketplace visibility
 *
 * Flow: PENDING_APPROVAL → ACTIVE → EXPIRED (or HIDDEN by owner)
 *       PENDING_APPROVAL → REJECTED (staff rejects) → owner edits → PENDING_APPROVAL again
 */
public enum PostStatus {
    /**
     * PENDING_APPROVAL: Post submitted by owner, waiting for staff review
     * - postExpiredAt is null (timer has not started yet)
     * - Not visible in marketplace
     * - Staff must approve before it goes live
     */
    PENDING_APPROVAL,

    /**
     * REJECTED: Staff rejected the post
     * - rejectionReason field contains staff feedback
     * - Owner can edit and resubmit (back to PENDING_APPROVAL)
     */
    REJECTED,

    /**
     * DRAFT: Post created but not yet paid/published
     * - Property exists but post has no duration yet
     * - Not visible in marketplace
     * - Waiting for owner to purchase POST package
     */
    DRAFT,
    
    /**
     * ACTIVE: Post is live and visible in marketplace
     * - Has valid postExpiredAt (not expired yet)
     * - Automatically set when owner purchases POST duration
     * - Condition: now < postExpiredAt
     */
    ACTIVE,
    
    /**
     * EXPIRED: Post duration has ended
     * - postExpiredAt has passed
     * - No longer visible in marketplace
     * - Can be renewed by purchasing new duration
     * - Automatically set by scheduler when now >= postExpiredAt
     */
    EXPIRED,
    
    /**
     * HIDDEN: Manually hidden by owner
     * - Owner chose to hide (not due to expiration)
     * - Can be unhidden anytime
     * - Example: Property already rented but owner wants to keep record
     */
    HIDDEN
}