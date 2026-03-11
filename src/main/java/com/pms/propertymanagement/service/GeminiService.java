package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.chat.GeminiActionResult;
import com.pms.propertymanagement.dto.chat.GeminiExtractResult;
import com.pms.propertymanagement.dto.chat.PropertySummaryForChat;
import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.model.ChatSessionState;

import java.util.List;

/**
 * Wraps Gemini API calls for the chat recommendation feature.
 *
 * Two entry points matching the two chat phases:
 *   Phase 1 (SLOT_FILLING)    → extractParams()
 *   Phase 2 (SHOWING_RESULTS) → detectAction()
 *
 * Caller is responsible for:
 *   - Validating IDs returned by Gemini against DB before using them
 *   - Handling exceptions thrown when the API is unreachable
 */
public interface GeminiService {

    /**
     * Phase 1: extract structured search parameters from a user message.
     *
     * Reference data lists (amenities, categories, etc.) are injected into the prompt
     * so Gemini can only return IDs that exist in the DB — prevents hallucination.
     *
     * @param userMessage     Raw message from the user
     * @param state           Current session state (known params shown as context)
     * @param amenities       All amenities from DB
     * @param surroundings    All surroundings from DB
     * @param targetTenants   All target tenant types from DB
     * @param categories      All property categories from DB
     * @param provinces       All provinces from DB (sent to prevent hallucinated codes)
     * @return Parsed result; never null — throws RuntimeException on API failure
     */
    GeminiExtractResult extractParams(
            String userMessage,
            ChatSessionState state,
            List<Amenity> amenities,
            List<Surrounding> surroundings,
            List<TargetTenant> targetTenants,
            List<Category> categories,
            List<Province> provinces);

    /**
     * Phase 2: detect user intent and generate a reply based on current results.
     *
     * @param userMessage     Raw message from the user
     * @param state           Current session state (budget, location, amenities)
     * @param cachedResults   Currently displayed property results (for context)
     * @param provinceName    Full province name (e.g. "Thành phố Hồ Chí Minh")
     * @param allAmenities    All amenities from DB (to resolve amenityId → name for prompt)
     * @return Parsed result; never null — throws RuntimeException on API failure
     */
    GeminiActionResult detectAction(
            String userMessage,
            ChatSessionState state,
            List<PropertySummaryForChat> cachedResults,
            String provinceName,
            List<Amenity> allAmenities);
}
