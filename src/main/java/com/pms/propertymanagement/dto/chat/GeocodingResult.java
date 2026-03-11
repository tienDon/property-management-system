package com.pms.propertymanagement.dto.chat;

/**
 * Result returned by GeocodingService after calling OpenCage API.
 *
 * confidence: 1-10 (higher = more precise)
 *   8-10 → building/POI level  → suggestedRadius = 3km
 *   5-7  → street/area level   → suggestedRadius = 5km
 *   < 5  → too vague           → isUsable() = false → fallback to ward search
 *
 * suburb: ward name from API components.suburb, may be null.
 */
public record GeocodingResult(
        double lat,
        double lng,
        int confidence,
        String suburb,             // ward name hint from API components.suburb, nullable
        String normalizedAddress   // formatted address from API, used to populate Property.normalizedAddress
) {
    /** Returns false when confidence < 5 — caller should fall back to ward search */
    public boolean isUsable() {
        return confidence >= 5;
    }
    // NOTE: radius is intentionally NOT computed here.
    // Callers must use GeocodingProperties.getRadius() to stay consistent with config.
}
