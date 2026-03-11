package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.config.GeocodingProperties;
import com.pms.propertymanagement.dto.chat.GeocodingResult;
import com.pms.propertymanagement.entity.Ward;
import com.pms.propertymanagement.model.ChatSessionState;
import com.pms.propertymanagement.repository.WardRepository;
import com.pms.propertymanagement.service.GeocodingService;
import com.pms.propertymanagement.service.LocationResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Converts Gemini-extracted location fields into DB-level ward codes
 * or geocoded latitude/longitude, stored back into ChatSessionState.
 *
 * Radius selection:
 *   confidence >= 8 (POI/building-level) → geocode.radius.confident  (default 3 km)
 *   confidence  5-7 (street/area-level)  → geocode.radius.approximate (default 5 km)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationResolverServiceImpl implements LocationResolverService {

    private final GeocodingService geocodingService;
    private final WardRepository wardRepository;
    private final GeocodingProperties geocodingProperties;

    @Override
    public void resolve(ChatSessionState state, String provinceName) {
        // mergeExtracted() in ChatController has already synced all non-null extracted fields
        // into state BEFORE this method is called.
        // Therefore, read from STATE — not from extracted — so that multi-turn slot-filling works:
        //   Turn 1: user gives location only → mergeExtracted writes locationType/localKeyword to state
        //           → hasRequiredFields() still false (no budget) → resolve() is never reached
        //   Turn 2: user gives budget only → Gemini returns locationType=null in this turn
        //           → if we read extracted.getLocationType(), we get null → return early → BUG
        //           → reading state.getLocationType() correctly finds the ward from turn 1
        String locationType = state.getLocationType();
        if (locationType == null) {
            log.debug("locationType is null — location left unchanged");
            return;
        }

        String provinceCode = state.getProvinceCode();

        switch (locationType) {
            case "ward"          -> resolveWard(state, state.getLocalKeyword(), provinceCode);
            case "landmark",
                 "street"        -> resolveLandmark(state, state.getPlaceName(), provinceName, provinceCode);
            case "province_only" -> clearLocationFilters(state);
            default              -> log.warn("Unknown locationType '{}' — location left unchanged", locationType);
        }
    }

    // =========================================================================
    // Resolvers
    // =========================================================================

    /**
     * Searches wards by partial name within the given province.
     * If multiple wards match (e.g. "Phường 1" in a large province) all are kept
     * so the DB query casts a wide net.
     * Falls back to province-wide search when no wards match.
     */
    private void resolveWard(ChatSessionState state, String localKeyword, String provinceCode) {
        if (localKeyword == null || provinceCode == null) {
            log.warn("Ward resolution skipped — localKeyword={} or provinceCode={} is null",
                    localKeyword, provinceCode);
            return;
        }

        List<Ward> wards = wardRepository.searchByNameInProvince(localKeyword, provinceCode);
        if (wards.isEmpty()) {
            log.debug("No wards matched keyword='{}' in province='{}' — falling back to province-wide",
                    localKeyword, provinceCode);
            state.setResolvedWardCodes(null);
        } else {
            List<String> codes = wards.stream().map(Ward::getCode).toList();
            log.debug("Resolved '{}' → {} ward(s): {}", localKeyword, codes.size(), codes);
            state.setResolvedWardCodes(codes);
        }

        // Ward-based search does not use coordinates
        clearCoordinates(state);
    }

    /**
     * Geocodes a place name (landmark or street address).
     * On success: stores lat/lng + radius derived from confidence level.
     * On failure: falls back to LIKE ward search using placeName as keyword.
     *   If ward search also finds nothing → province-wide search.
     */
    private void resolveLandmark(ChatSessionState state, String placeName,
                                  String provinceName, String provinceCode) {
        if (placeName == null || provinceName == null) {
            log.warn("Landmark resolution skipped — placeName={} or provinceName={} is null",
                    placeName, provinceName);
            return;
        }

        GeocodingResult result = geocodingService.geocode(placeName, provinceName);
        if (result == null || !result.isUsable()) {
            // Geocoding failed → fallback: LIKE search ward from placeName (per design spec)
            log.warn("Geocoding failed for placeName='{}' — falling back to ward LIKE search", placeName);
            resolveWard(state, placeName, provinceCode);
            return;
        }

        double radius = result.confidence() >= 8
                ? geocodingProperties.getRadius().getConfident()
                : geocodingProperties.getRadius().getApproximate();

        state.setCenterLat(result.lat());
        state.setCenterLng(result.lng());
        state.setSearchRadiusKm(radius);
        // Bounding-box search does not use ward codes
        state.setResolvedWardCodes(null);

        log.debug("Geocoded '{}' → lat={}, lng={}, confidence={}, radius={}km",
                placeName, result.lat(), result.lng(), result.confidence(), radius);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Resets all location-specific state → next DB query will scan the whole province */
    private void clearLocationFilters(ChatSessionState state) {
        state.setResolvedWardCodes(null);
        clearCoordinates(state);
        log.debug("Location filters cleared → province-wide search");
    }

    private void clearCoordinates(ChatSessionState state) {
        state.setCenterLat(null);
        state.setCenterLng(null);
        state.setSearchRadiusKm(null);
    }
}
