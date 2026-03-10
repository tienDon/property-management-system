package com.pms.propertymanagement.dto.chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flattened property snapshot stored in HttpSession (cachedResults) and sent to Gemini.
 *
 * CRITICAL: Contains only plain Java types — no Hibernate entities or proxies.
 * Storing entities in HttpSession causes LazyInitializationException on deserialization.
 *
 * Fields sent to Gemini: displayIndex, price, area, maxOccupancy, wardName,
 *   provinceName, postTitle, postSlug, amenityNames, surroundingNames, categoryName.
 *
 * Internal-only fields (not sent to Gemini, used for scoring/ranking):
 *   latitude, longitude, boosted, amenityMatchCount, wardMatch, postExpiredAt.
 *
 * Note: boolean components in records use accessor name without "is" prefix.
 *   Use r.boosted() and r.wardMatch() in scoring code.
 */
public record PropertySummaryForChat(
        int displayIndex,           // 1-based, for Gemini to reference
        Long postId,                // for sort tie-breaker (older post = lower id)
        Double price,               // cheapest available room price
        Double area,                // room area m²
        Integer maxOccupancy,
        String wardName,
        String provinceName,
        String postTitle,
        String postSlug,            // for redirect URL /properties/{slug}
        List<String> amenityNames,
        List<String> surroundingNames,
        String categoryName,

        // --- Scoring fields (not sent to Gemini) ---
        Double latitude,            // null if property not yet geocoded
        Double longitude,
        boolean boosted,            // post has active boostExpiredAt
        int amenityMatchCount,      // how many requested amenities this property has
        boolean wardMatch,          // property ward is in resolvedWardCodes
        LocalDateTime postExpiredAt // for post-longevity score
) implements Serializable {}
