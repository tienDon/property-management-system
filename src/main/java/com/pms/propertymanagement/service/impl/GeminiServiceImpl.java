package com.pms.propertymanagement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.propertymanagement.config.GeminiProperties;
import com.pms.propertymanagement.dto.chat.GeminiActionResult;
import com.pms.propertymanagement.dto.chat.GeminiExtractResult;
import com.pms.propertymanagement.dto.chat.PropertySummaryForChat;
import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.model.ChatSessionState;
import com.pms.propertymanagement.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calls Google Gemini 1.5 Flash API to extract search params (Phase 1)
 * and detect user actions (Phase 2).
 *
 * API endpoint: POST {gemini.api.url}?key={gemini.api.key}
 * generationConfig.responseMimeType = "application/json" forces pure-JSON output,
 * eliminating the need to strip markdown code fences.
 *
 * Throws RuntimeException on API failure — caller (ChatController) should catch
 * and return a user-friendly error message.
 */
@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    private final GeminiProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public GeminiServiceImpl(GeminiProperties props) {
        this.props = props;
        // Apply connect + read timeouts from application.properties
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeout().getConnect()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(props.getTimeout().getRead()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getApi().getUrl())
                .requestFactory(requestFactory)
                .build();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    @Override
    public GeminiExtractResult extractParams(String userMessage, ChatSessionState state,
                                              List<Amenity> amenities, List<Surrounding> surroundings,
                                              List<TargetTenant> targetTenants, List<Category> categories,
                                              List<Province> provinces) {
        String prompt = buildPhase1Prompt(userMessage, state,
                toIdNameJson(categories, Category::getId, Category::getName),
                toIdNameJson(amenities, Amenity::getId, Amenity::getName),
                toIdNameJson(surroundings, Surrounding::getId, Surrounding::getName),
                toIdNameJson(targetTenants, TargetTenant::getId, TargetTenant::getName),
                toCodeNameJson(provinces));

        String raw = callGemini(prompt);
        try {
            GeminiExtractResult result = objectMapper.readValue(raw, GeminiExtractResult.class);
            log.debug("Gemini extractParams intent={}, missing={}", result.getIntent(), result.getMissingRequired());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini extractParams response: {}", raw, e);
            throw new RuntimeException("Gemini response parse error (extractParams)", e);
        }
    }

    @Override
    public GeminiActionResult detectAction(String userMessage, ChatSessionState state,
                                            List<PropertySummaryForChat> cachedResults,
                                            String provinceName, List<Amenity> allAmenities) {
        String prompt = buildPhase2Prompt(userMessage, state, cachedResults, provinceName, allAmenities);

        String raw = callGemini(prompt);
        try {
            GeminiActionResult result = objectMapper.readValue(raw, GeminiActionResult.class);
            log.debug("Gemini detectAction action={}", result.getAction());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini detectAction response: {}", raw, e);
            throw new RuntimeException("Gemini response parse error (detectAction)", e);
        }
    }

    // =========================================================================
    // Gemini API call
    // =========================================================================

    /**
     * Sends a single-turn prompt to Gemini and returns the raw text response.
     * responseMimeType="application/json" ensures clean JSON output with no markdown.
     */
    private String callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 1024,
                        "responseMimeType", "application/json"
                )
        );

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("key", props.getApi().getKey())
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API unreachable: " + e.getMessage(), e);
        }

        if (responseBody == null) {
            throw new RuntimeException("Gemini API returned null body");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            // Safety strip — responseMimeType should prevent this, but just in case
            text = text.trim();
            if (text.startsWith("```")) {
                text = text.replaceAll("(?s)^```[a-z]*\\n?", "").replaceAll("\\n?```$", "").trim();
            }
            return text;
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response: {}", responseBody, e);
            throw new RuntimeException("Unexpected Gemini response structure", e);
        }
    }

    // =========================================================================
    // Prompt builders
    // =========================================================================

    private String buildPhase1Prompt(String userMessage, ChatSessionState state,
                                      String categoriesJson, String amenitiesJson,
                                      String surroundingsJson, String targetTenantsJson,
                                      String provincesJson) {
        return """
                Bạn là trợ lý tìm phòng trọ tại Việt Nam.
                
                === DỮ LIỆU HỆ THỐNG ===
                LOẠI BĐS:   %s
                TIỆN ÍCH:   %s
                XUNG QUANH: %s
                ĐỐI TƯỢNG:  %s
                TỈNH/THÀNH: %s
                
                === THÔNG TIN ĐÃ BIẾT ===
                Ngân sách:      %s
                Tỉnh/Thành:     %s
                Khu vực cụ thể: %s
                Loại BĐS:       %s
                Tiện ích:       %s
                
                === HỘI THOẠI GẦN ĐÂY (4 turn) ===
                %s
                
                === TIN NHẮN MỚI ===
                User: %s
                
                Phân tích và trả về JSON hợp lệ duy nhất, không markdown, không giải thích:
                {
                  "intent": "find_room | off_topic | ambiguous",
                  "extracted": {
                    "budget": <số VNĐ hoặc null>,
                    "provinceCode": "<code từ danh sách TỈNH/THÀNH ở trên, hoặc null>",
                    "locationType": "ward | landmark | street | province_only | null",
                    "localKeyword": "<tên phường có dấu — chỉ khi locationType=ward, null nếu không>",
                    "placeName": "<tên địa điểm đầy đủ — chỉ khi locationType=landmark/street, null nếu không>",
                    "categoryId": <id hoặc null>,
                    "amenityIds": [<id>],
                    "surroundingIds": [<id>],
                    "targetTenantId": <id hoặc null>
                  },
                  "missingRequired": ["budget" | "location"],
                  "clarificationNeeded": "<câu hỏi nếu intent=ambiguous, null nếu không>"
                }
                
                Quy tắc locationType:
                - "ward": user nhắc đến tên phường/xã/khu vực hành chính → localKeyword = tên có dấu (không cần tiền tố "Phường"), placeName = null
                - "landmark": địa điểm cụ thể (trường ĐH, bệnh viện, TTTM, KCN) → placeName = tên đầy đủ để gọi Geocoding API, localKeyword = null
                - "street": tên đường hoặc địa chỉ cụ thể → placeName = tên đường (thêm khu vực nếu có), localKeyword = null
                - "province_only": user chỉ nói tỉnh/thành, không nhắc khu vực → cả hai null
                - provinceCode phải lấy từ danh sách TỈNH/THÀNH ở trên, không tự bịa
                - Việt Nam hiện chỉ có 2 cấp: Tỉnh/Thành phố → Phường/Xã (không còn quận/huyện)
                """.formatted(
                categoriesJson, amenitiesJson, surroundingsJson, targetTenantsJson, provincesJson,
                formatBudget(state.getBudget()),
                state.getProvinceCode() != null ? state.getProvinceCode() : "chưa biết",
                formatKnownLocation(state),
                state.getCategoryId() != null ? state.getCategoryId().toString() : "chưa biết",
                state.getAmenityIds() != null ? state.getAmenityIds().toString() : "chưa biết",
                formatHistory(state.getHistoryAsList()),
                userMessage
        );
    }

    private String buildPhase2Prompt(String userMessage, ChatSessionState state,
                                      List<PropertySummaryForChat> cachedResults,
                                      String provinceName, List<Amenity> allAmenities) {
        String amenityNames = resolveAmenityNames(state.getAmenityIds(), allAmenities);
        String location = formatKnownLocation(state);
        String resultsJson = buildCachedResultsJson(cachedResults);
        String amenityListJson = toIdNameJson(allAmenities, Amenity::getId, Amenity::getName);

        return """
                Bạn là trợ lý tìm phòng trọ.
                
                === THÔNG TIN TÌM KIẾM HIỆN TẠI ===
                Ngân sách: %s | Tỉnh/Thành: %s | Khu vực: %s | Tiện ích: %s
                
                === KẾT QUẢ HIỆN TẠI ===
                %s
                
                === DANH SÁCH TIỆN ÍCH HỢP LỆ (chỉ dùng id số nguyên từ danh sách này) ===
                %s
                
                === HỘI THOẠI GẦN ĐÂY ===
                %s
                
                User: %s
                
                Trả về JSON hợp lệ duy nhất, không markdown, không giải thích:
                {
                  "action": "refine | new_search | question | interest | exit",
                  "refinement": {
                    "newBudget": null,
                    "addAmenityIds": [],
                    "removeAmenityIds": [],
                    "newCategoryId": null
                  },
                  "targetPropertyIndex": <số 1-based nếu action=interest, null nếu không>,
                  "reply": "<câu trả lời tự nhiên tiếng Việt>"
                }
                
                Quy tắc:
                - refine: user điều chỉnh filter (budget/amenity/category) NHƯNG KHÔNG đổi tỉnh/thành/khu vực
                  Ví dụ refine: "thêm tiện ích", "tìm thêm tiện ích", "thêm máy lạnh", "bớt ngân sách", "tìm loại khác"
                - new_search: user đổi hẳn tỉnh/thành hoặc khu vực địa lý sang một nơi KHÁC hoàn toàn
                  Ví dụ new_search: "tìm ở Hà Nội đi", "đổi sang Đà Nẵng", "thử quận 1 xem"
                  KHÔNG phải new_search nếu user chỉ nói "tìm thêm tiện ích" hay "thêm yêu cầu" mà không đổi địa điểm
                - question: trả lời dựa hoàn toàn trên KẾT QUẢ HIỆN TẠI, không query DB thêm
                - interest: targetPropertyIndex = số thứ tự trong danh sách (1, 2, 3...)
                - exit: user không muốn tìm nữa
                - addAmenityIds / removeAmenityIds: PHẢI là mảng số nguyên (Long) lấy từ "id" trong DANH SÁCH TIỆN ÍCH. Tuyệt đối không dùng tên chuỗi.
                - newCategoryId: PHẢI là số nguyên (Long) hoặc null.
                """.formatted(
                formatBudget(state.getBudget()), provinceName, location, amenityNames,
                resultsJson,
                amenityListJson,
                formatHistory(state.getHistoryAsList()),
                userMessage
        );
    }

    // =========================================================================
    // Serialization helpers
    // =========================================================================

    /**
     * Serializes a list to JSON with only "id" and "name" keys.
     * Used for amenities, categories, surroundings, targetTenants in Phase 1 prompt.
     */
    private <T> String toIdNameJson(List<T> items,
                                     Function<T, Long> idFn,
                                     Function<T, String> nameFn) {
        List<Map<String, Object>> list = items.stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", idFn.apply(item));
                    map.put("name", nameFn.apply(item));
                    return map;
                })
                .toList();
        return writeJson(list);
    }

    /**
     * Serializes provinces to JSON with "code" and "name" keys (not "id").
     */
    private String toCodeNameJson(List<Province> provinces) {
        List<Map<String, Object>> list = provinces.stream()
                .map(p -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("code", p.getCode());
                    map.put("name", p.getName());
                    return map;
                })
                .toList();
        return writeJson(list);
    }

    /**
     * Serializes only the display-relevant fields of cachedResults for Phase 2 prompt.
     * Excludes scoring fields (lat/lng/boosted/etc.) to reduce token usage.
     */
    private String buildCachedResultsJson(List<PropertySummaryForChat> results) {
        List<Map<String, Object>> list = results.stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("no", r.displayIndex());
                    map.put("title", r.postTitle());
                    map.put("price", r.price());
                    map.put("area", r.area());
                    map.put("ward", r.wardName());
                    map.put("amenities", r.amenityNames());
                    return map;
                })
                .toList();
        return writeJson(list);
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON serialization failed: {}", e.getMessage());
            return "[]";
        }
    }

    // =========================================================================
    // Prompt formatting helpers
    // =========================================================================

    private String formatBudget(Double budget) {
        if (budget == null) return "chưa biết";
        return String.format("%.0f VNĐ/tháng", budget);
    }

    private String formatKnownLocation(ChatSessionState state) {
        if (state.getPlaceName() != null) return state.getPlaceName();
        if (state.getLocalKeyword() != null) return state.getLocalKeyword();
        return "chưa biết";
    }

    private String resolveAmenityNames(List<Long> amenityIds, List<Amenity> allAmenities) {
        if (amenityIds == null || amenityIds.isEmpty()) return "không có";
        Set<Long> idSet = new HashSet<>(amenityIds);
        String names = allAmenities.stream()
                .filter(a -> idSet.contains(a.getId()))
                .map(Amenity::getName)
                .collect(Collectors.joining(", "));
        return names.isEmpty() ? "không có" : names;
    }

    private String formatHistory(List<ChatSessionState.ConversationTurn> history) {
        if (history.isEmpty()) return "(chưa có hội thoại)";
        StringBuilder sb = new StringBuilder();
        for (var turn : history) {
            String role = "model".equals(turn.role()) ? "Bot" : "User";
            sb.append(role).append(": ").append(turn.content()).append("\n");
        }
        return sb.toString().trim();
    }
}
