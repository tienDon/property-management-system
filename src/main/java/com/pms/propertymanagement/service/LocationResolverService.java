package com.pms.propertymanagement.service;

import com.pms.propertymanagement.model.ChatSessionState;

/**
 * Resolves Gemini-extracted location fields into ward codes or geocoded coordinates,
 * then updates ChatSessionState in-place.
 *
 * Decision table based on locationType:
 *   "ward"          → WardRepository.searchByNameInProvince → set resolvedWardCodes
 *   "landmark"      → GeocodingService.geocode              → set centerLat/Lng/searchRadiusKm
 *   "street"        → GeocodingService.geocode              → set centerLat/Lng/searchRadiusKm
 *   "province_only" → clear all location filters            → query whole province
 *   null / unknown  → no-op; caller keeps existing state
 *
 * Fallback when geocoding fails (landmark/street): clears all location filters
 * → PropertyRecommendationService will do a province-wide relaxed search.
 */
public interface LocationResolverService {

    /**
     * Resolves location and mutates {@code state} in-place.
     * Reads all required fields (locationType, localKeyword, placeName, provinceCode)
     * directly from {@code state} — callers must call mergeExtracted() before this.
     *
     * @param state        Session state to read from and update
     * @param provinceName Full province display name (e.g. "Thành phố Hồ Chí Minh"),
     *                     required for constructing the geocoding query
     */
    void resolve(ChatSessionState state, String provinceName);
}
