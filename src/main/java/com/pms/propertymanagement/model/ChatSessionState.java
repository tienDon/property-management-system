package com.pms.propertymanagement.model;

import com.pms.propertymanagement.dto.chat.PropertySummaryForChat;
import com.pms.propertymanagement.enums.ChatPhase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Per-user chat session state stored in HttpSession.
 *
 * Lifecycle: created on first chat message, expires after session timeout (30 min idle).
 * Must implement Serializable — Tomcat may serialize sessions on restart.
 *
 * Important null semantics (JPQL relies on these):
 *   resolvedWardCodes = null → query toàn tỉnh (JPQL: :wardCodes IS NULL → skip ward filter)
 *   amenityIds        = null → không lọc amenity (JPQL: :amenityIds IS NULL → skip amenity filter)
 *   Do NOT initialize these lists to new ArrayList<>() — that breaks the IS NULL check.
 */
@Getter
@Setter
public class ChatSessionState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // guestId is immutable after construction — no setter
    @Setter(lombok.AccessLevel.NONE)
    private final String guestId;

    // === Required fields (must have to query DB) ===
    private Double budget;
    private String provinceCode;

    // === Location fields (set by LocationResolverService after Gemini extracts) ===
    private String locationType;       // ward | landmark | street | province_only
    private String placeName;          // full location name (for locationType=landmark/street)
    private String localKeyword;       // ward name keyword (for locationType=ward)

    // null = query toàn tỉnh; KHÔNG khởi tạo ArrayList — JPQL dùng ":wardCodes IS NULL"
    private List<String> resolvedWardCodes = null;

    private Double centerLat;          // geocoded center (for bounding box + Haversine)
    private Double centerLng;
    private Double searchRadiusKm;

    // === Optional filters ===
    private Long categoryId;
    private Long targetTenantId;

    // null = không lọc; KHÔNG khởi tạo ArrayList — JPQL dùng ":amenityIds IS NULL"
    private List<Long> amenityIds = null;
    private List<Long> surroundingIds = null;

    // === Phase ===
    private ChatPhase phase = ChatPhase.SLOT_FILLING;

    // === REFINING sub-state ===
    // false = collecting optional filters; true = showing confirm summary
    private boolean refiningConfirmStep = false;

    // Flattened snapshots — NOT Hibernate entities (would cause LazyInitializationException)
    private List<PropertySummaryForChat> cachedResults = new ArrayList<>();

    // === Conversation history (sliding window, max 4 turns) ===
    // Excluded from Lombok @Getter/@Setter — accessed via dedicated methods
    private final Deque<ConversationTurn> history = new ArrayDeque<>();
    private static final int MAX_HISTORY_TURNS = 4;

    // === Rate limiting (tracked per session) ===
    private int requestCount = 0;
    private long rateLimitWindowStart = System.currentTimeMillis();

    public ChatSessionState(String guestId) {
        this.guestId = guestId;
    }

    public void addTurn(String role, String content) {
        history.addLast(new ConversationTurn(role, content));
        while (history.size() > MAX_HISTORY_TURNS) {
            history.removeFirst();
        }
    }

    /** Returns a snapshot list of the history (safe to iterate; not a live view) */
    public List<ConversationTurn> getHistoryAsList() {
        return new ArrayList<>(history);
    }

    /**
     * Resets all mutable search fields to their initial default values.
     * Called by new_search flow so the same object reference is reused —
     * the outer handleMessage() can then safely call session.setAttribute(state)
     * without needing a new object.
     */
    public void reset() {
        budget          = null;
        provinceCode    = null;
        locationType    = null;
        placeName       = null;
        localKeyword    = null;
        resolvedWardCodes = null;
        centerLat       = null;
        centerLng       = null;
        searchRadiusKm  = null;
        categoryId      = null;
        targetTenantId  = null;
        amenityIds      = null;
        surroundingIds  = null;
        phase           = ChatPhase.SLOT_FILLING;
        refiningConfirmStep = false;
        cachedResults   = new ArrayList<>();
        history.clear();
        requestCount    = 0;
        rateLimitWindowStart = System.currentTimeMillis();
    }

    /** budget + provinceCode must both be present before querying DB */
    public boolean hasRequiredFields() {
        return budget != null && provinceCode != null;
    }

    /** True when geocoding succeeded → use Haversine instead of ward filter */
    public boolean hasCoordinates() {
        return centerLat != null && centerLng != null;
    }

    /**
     * Conversation turn stored in sliding-window history.
     * role: "user" | "model" (Gemini convention)
     */
    public record ConversationTurn(String role, String content) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
