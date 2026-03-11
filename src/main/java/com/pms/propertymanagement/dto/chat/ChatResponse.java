package com.pms.propertymanagement.dto.chat;

import java.util.List;

/**
 * JSON response returned by ChatController to the frontend.
 *
 * message:      Bot reply text, always present.
 * results:      New property results to render; null when no new results.
 * postSlug:     Populated only on action=interest — frontend highlights the matching card.
 * resetChat:    true on action=exit — frontend clears the chat panel.
 * quickReplies: Optional list of chip labels the user can tap to auto-send.
 */
public record ChatResponse(
        String message,
        List<PropertySummaryForChat> results,
        String postSlug,
        boolean resetChat,
        List<String> quickReplies
) {
    /** Text-only reply */
    public static ChatResponse ofMessage(String message) {
        return new ChatResponse(message, null, null, false, null);
    }

    /** Text reply with quick-reply chips */
    public static ChatResponse ofMessageWithReplies(String message, List<String> quickReplies) {
        return new ChatResponse(message, null, null, false, quickReplies);
    }

    /** Result reply — shows new property cards */
    public static ChatResponse ofResults(String message, List<PropertySummaryForChat> results) {
        return new ChatResponse(message, results, null, false, null);
    }

    /** Interest reply — frontend should highlight the card with this slug */
    public static ChatResponse ofInterest(String message, String postSlug) {
        return new ChatResponse(message, null, postSlug, false, null);
    }

    /** Exit reply — frontend clears the chat */
    public static ChatResponse ofExit(String message) {
        return new ChatResponse(message, null, null, true, null);
    }
}
