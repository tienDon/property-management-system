package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.chat.GeocodingResult;

/**
 * Calls OpenCage Geocoding API to convert a place name into coordinates.
 *
 * Used in two flows:
 *   1. Chat flow  — resolve landmark/street address → lat/lng for Haversine search
 *   2. Property form flow — geocode owner's address → store lat/lng + normalizedAddress on Property
 */
public interface GeocodingService {

    /**
     * Geocode a place name within a province context.
     *
     * Query sent to API: "{placeName}, {provinceName}, Việt Nam"
     * Adding provinceName disambiguates multi-city landmarks (e.g. FPT University).
     *
     * @param placeName    The landmark, street, or address string to geocode
     * @param provinceName Full province name (e.g. "Thành phố Hồ Chí Minh")
     * @return GeocodingResult when API returns usable result (confidence >= min),
     *         null when API fails, returns no results, or confidence is too low
     */
    GeocodingResult geocode(String placeName, String provinceName);
}
