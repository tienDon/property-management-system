package com.pms.propertymanagement.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Deserialized response from Gemini Phase 2 (detectAction).
 *
 * action:
 *   "refine"      — adjust filters on same location (budget, amenity, category)
 *   "new_search"  — user changed location/province → server resets state → back to Phase 1
 *   "question"    — answer from cachedResults, no DB query needed
 *   "interest"    — user interested in a specific result; targetPropertyIndex is set
 *   "exit"        — user done; server clears session
 *
 * reply: always present — the bot's natural language response to show the user.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiActionResult {

    private String action;               // "refine" | "new_search" | "question" | "interest" | "exit"
    private Refinement refinement;
    private Integer targetPropertyIndex; // 1-based index into cachedResults; non-null only when action=interest
    private String reply;

    /**
     * Filter adjustments requested by user.
     * Only relevant when action = "refine".
     * All fields may be null if not mentioned.
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Refinement {
        private Double newBudget;
        private List<Long> addAmenityIds;
        private List<Long> removeAmenityIds;
        private Long newCategoryId;
    }
}
