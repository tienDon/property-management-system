package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.chat.PropertySummaryForChat;
import com.pms.propertymanagement.model.ChatSessionState;

import java.util.List;

/**
 * Queries the DB for matching properties based on ChatSessionState,
 * applies relaxed search fallback if needed, scores and ranks results.
 *
 * Called on:
 *   Phase 1 → first time required fields are complete
 *   Phase 2 → action=refine (re-query with updated params, same location)
 *
 * Returns at most chat.recommendation.max-results items (default 5).
 */
public interface PropertyRecommendationService {

    /**
     * Finds and ranks property results for the given session state.
     *
     * Strategy:
     *   1. Strict query (budget + location + amenity + category)
     *   2. If 0 results → relaxed budget (×1.2) + drop amenity filter
     *   3. If still 0 → province-wide cheapest (drop all filters)
     *
     * Result list is scored/ranked before being returned.
     * displayIndex is assigned (1-based) after ranking.
     *
     * @param state  Current session state; must have budget + provinceCode set
     * @return       Ranked list (may be empty if truly no rooms in the province)
     */
    List<PropertySummaryForChat> findRecommendations(ChatSessionState state);

    /**
     * Quick COUNT query — province + budget only, no other filters.
     * Used by REFINING phase to decide how much narrowing is needed.
     */
    long countProvinceResults(ChatSessionState state);
}
