package com.pms.propertymanagement.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Deserialized response from Gemini Phase 1 (extractParams).
 *
 * intent:
 *   "find_room"  — user wants to find a room, proceed with extraction
 *   "off_topic"  — unrelated message, reply with static template
 *   "ambiguous"  — insufficient info to proceed, ask clarificationNeeded
 *
 * missingRequired: list of missing required fields: "budget" | "location"
 * clarificationNeeded: non-null only when intent = "ambiguous"
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiExtractResult {

    private String intent;                  // "find_room" | "off_topic" | "ambiguous"
    private Extracted extracted;
    private List<String> missingRequired;
    private String clarificationNeeded;

    /** True when Gemini found all required fields → ready to query DB */
    public boolean isComplete() {
        return "find_room".equals(intent)
                && (missingRequired == null || missingRequired.isEmpty());
    }

    /**
     * Extracted parameters from user message.
     * Fields may be null when not mentioned by the user.
     * amenityIds / surroundingIds may be null (not []) — handle in validation layer.
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extracted {
        private Double budget;
        private String provinceCode;   // constrained to province list sent in prompt
        private String locationType;   // "ward" | "landmark" | "street" | "province_only"
        private String localKeyword;   // ward name, non-null only when locationType=ward
        private String placeName;      // full place name, non-null only when locationType=landmark/street
        private Long categoryId;
        private List<Long> amenityIds;
        private List<Long> surroundingIds;
        private Long targetTenantId;
    }
}
