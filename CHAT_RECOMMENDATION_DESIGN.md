# Chat Gợi Ý Tìm Nhà Trọ — Thiết Kế Hệ Thống

> Tài liệu này mô tả toàn bộ luồng, kịch bản, kiến trúc và quyết định thiết kế
> cho tính năng chat AI gợi ý tìm phòng trọ tích hợp Gemini API.

---

## 1. Tổng Quan Tính Năng

User mở chat bubble trên trang public → mô tả nhu cầu bằng ngôn ngữ tự nhiên → hệ thống dùng Gemini để hiểu yêu cầu → query DB → trả về danh sách phòng phù hợp kèm so sánh ưu/nhược điểm.

**Ví dụ:**

> "Tôi là sinh viên, tài chính 5 triệu, muốn tìm phòng gần ĐH FPT HCM, cần có điều hòa"
> → Gemini extract params → query DB → trả về 3-5 phòng phù hợp

---

## 2. Tech Stack

| Thành phần              | Lựa chọn                       | Lý do                                                                                                                                                   |
| ----------------------- | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| LLM                     | Google Gemini 1.5 Flash        | Free tier 15 req/min, hiểu tiếng Việt tốt                                                                                                               |
| HTTP Client             | Spring `RestClient` (Spring 6) | Có sẵn trong Spring Boot 3                                                                                                                              |
| Session state           | Spring `HttpSession`           | Single-instance monolith → truy xuất từ RAM, độ trễ ≈ 0. Redis giải quyết Distributed Session (multi-node) — overkill cho project này, lãng phí RAM VPS |
| Geocoding               | OpenCage Geocoding API         | Free 2500 req/ngày, không cần billing, phù hợp project học tập                                                                                          |
| Chat UI                 | Vanilla JS + Tailwind          | Không cần thêm dependency                                                                                                                               |
| Persistent client state | `localStorage`                 | guestId + extracted params                                                                                                                              |

---

## 3. Kiến Trúc Tổng Quan

```
User nhập chat
      ↓
[ChatController] POST /api/chat/message
      ↓
Đọc ChatSessionState từ HttpSession
      ↓
      ├── Phase 1: SLOT_FILLING
      │     ↓
      │   GeminiService.extractParams()
      │     ↓ trả về locationType + placeName + provinceCode + ...
      │     ↓
      │   LocationResolverService.resolve()
      │     ├── locationType=ward       → LIKE search Ward DB → wardCodes
      │     ├── locationType=landmark   → Geocoding API → lat/lng + Haversine
      │     ├── locationType=street     → Geocoding API → lat/lng + Haversine
      │     └── locationType=province_only → toàn tỉnh
      │     ↓
      │   PropertyRecommendationService.query()
      │     ↓
      │   ChatResponse → Frontend render
      │
      └── Phase 2: SHOWING_RESULTS → GeminiService.detectAction() → handle action
                                            ↓ (nếu refine)
                              PropertyRecommendationService.query() (params mới, giữ nguyên location)
                                            ↓
                              ChatResponse → Frontend render
```

---

## 4. Các Class Cần Tạo Mới

### Backend

| Class                           | Package      | Chức năng                                                 |
| ------------------------------- | ------------ | --------------------------------------------------------- |
| `ChatController`                | `controller` | REST `POST /api/chat/message`                             |
| `GeminiService`                 | `service`    | Gọi Gemini API, build prompt, parse response              |
| `LocationResolverService`       | `service`    | Phân loại locationType → ward search hoặc Geocoding       |
| `GeocodingService`              | `service`    | Gọi OpenCage Geocoding API, parse kết quả, fallback logic |
| `PropertyRecommendationService` | `service`    | Query DB theo params, relaxed search, scoring             |
| `ChatSessionState`              | `model`      | Lưu trong HttpSession                                     |
| `PropertySummaryForChat`        | `dto`        | Flatten property data để gửi lên Gemini                   |
| `ChatResponse`                  | `dto`        | Response trả về frontend                                  |
| `GeminiProperties`              | `config`     | `@ConfigurationProperties` cho Gemini API                 |
| `GeocodingProperties`           | `config`     | `@ConfigurationProperties` cho OpenCage API               |

### Frontend

- Floating chat button trong `public-main.html`
- Chat panel popup (Vanilla JS)
- AJAX call đến `/api/chat/message`

---

## 5. Storage Strategy

> **Thiết kế Hybrid:** Client (localStorage) gánh dữ liệu dài hạn; Server (HttpSession) chỉ gánh state ngắn hạn ngay phiên làm việc.
> Nhờ vậy, Tomcat không phải lưu history qua nhiều ngày và `ChatSessionState` chỉ tốn vài KB mỗi user (primitive types + `List<Long>` + Deque 4 turns).

| Data                                   | Nơi lưu                        | TTL          | Lý do                                                                         |
| -------------------------------------- | ------------------------------ | ------------ | ----------------------------------------------------------------------------- |
| `guestId`                              | `localStorage`                 | Vĩnh viễn    | Định danh session, tạo 1 lần dùng mãi; không cần server lưu                   |
| Extracted params (budget, location...) | `localStorage` (kèm timestamp) | 7 ngày       | Tái sử dụng lần sau mà không cần hỏi lại                                      |
| Tin nhắn hiển thị                      | JS memory (array)              | Tab lifetime | Tránh stale data, đóng tab là reset                                           |
| `ChatSessionState` (server)            | `HttpSession`                  | 30 phút idle | Gemini context + `cachedResults`; JSESSIONID cookie tự đính kèm mọi AJAX call |

```js
// Client: lưu params kèm TTL
localStorage.setItem(
  "chatParams",
  JSON.stringify({
    data: {
      budget: 5000000,
      targetTenantId: 1,
      provinceCode: "79",
      locationType: "landmark",
      placeName: "Đại học FPT Hồ Chí Minh",
    },
    savedAt: Date.now(),
    ttl: 7 * 24 * 60 * 60 * 1000, // 7 ngày
  }),
);
```

---

## 6. ChatSessionState

```java
public class ChatSessionState {
    private String guestId;

    // === Required fields ===
    private Double budget;
    private String provinceCode;       // constrained: lấy từ danh sách province trong DB

    // === Location fields (sau khi Gemini extract) ===
    private String locationType;       // ward | landmark | street | province_only  ← từ Gemini extract; "ambiguous" là intent, không phải locationType
    private String placeName;          // tên địa điểm gốc Gemini trả về (VD: "Đại học FPT HCM")
    private String localKeyword;       // tên phường (chỉ dùng khi locationType=ward)
    private List<String> resolvedWardCodes = null; // null = query toàn tỉnh; KHÔNG khởi tạo ArrayList vì JPQL dùng ":wardCodes IS NULL" để bỏ filter
    private Double centerLat;          // tọa độ trung tâm (khi locationType=landmark/street)
    private Double centerLng;
    private Double searchRadiusKm;     // bán kính tìm kiếm (default 3km)

    // === Optional fields ===
    private Long categoryId;           // 1=Nhà trọ, 2=Nhà nguyên căn, 3=KTX, 4=Căn hộ
    private Long targetTenantId;
    private List<Long> amenityIds = null;        // null = không lọc amenity; KHÔNG khởi tạo ArrayList vì JPQL dùng ":amenityIds IS NULL" để bỏ filter
    private List<Long> surroundingIds = null;    // null = không lọc surrounding (hiện chưa dùng trong query)

    // === Phase tracking ===
    private ChatPhase phase;           // SLOT_FILLING | SHOWING_RESULTS
    private List<PropertySummaryForChat> cachedResults = new ArrayList<>();

    // === Conversation history (sliding window) ===
    private final Deque<ConversationTurn> history = new ArrayDeque<>();
    private static final int MAX_HISTORY_TURNS = 4;

    public void addTurn(String role, String content) {
        history.addLast(new ConversationTurn(role, content));
        while (history.size() > MAX_HISTORY_TURNS) {
            history.removeFirst();
        }
    }

    /**
     * Chỉ cần provinceCode là đủ để query.
     * locationType = null nghĩa là chưa biết khu vực cụ thể → query toàn tỉnh.
     */
    public boolean hasRequiredFields() {
        return budget != null && provinceCode != null;
    }

    /** Đã có tọa độ trung tâm → dùng Haversine thay vì ward filter */
    public boolean hasCoordinates() {
        return centerLat != null && centerLng != null;
    }

    public record ConversationTurn(String role, String content) {}
}
```

---

## 7. Required vs Optional Fields

```
Required (phải có để query DB):
  ├─ budget              → hỏi nếu thiếu
  └─ provinceCode        → constrained từ danh sách DB, hỏi nếu thiếu
                           (Gemini extract trực tiếp trong Prompt 1)

Optional (có thì tốt hơn, không có vẫn query được):
  ├─ localKeyword        → null = query toàn tỉnh (rộng hơn nhưng không miss)
  ├─ categoryId          → null = tìm tất cả loại
  ├─ amenityIds          → null = không lọc theo tiện ích
  ├─ surroundingIds      → null = không lọc theo xung quanh
  └─ targetTenantId      → null = không lọc theo đối tượng
```

**Xử lý location sau khi Gemini extract — do `LocationResolverService` đảm nhiệm:**

```
locationType = "ward"
  → LIKE search Ward DB theo localKeyword + provinceCode
  → found → state.resolvedWardCodes
  → not found → fallback: toàn tỉnh

locationType = "landmark" | "street"
  → GeocodingService.geocode(placeName, provinceName)
  → confidence ≥ 8 → state.centerLat/Lng, radius=3km  (building/POI level)
  → confidence 5-7 → state.centerLat/Lng, radius=5km  (street/area level)
  → confidence < 5 / fail → fallback: LIKE search ward từ placeName

locationType = "province_only"
  → không set wardCodes, không set centerLat/Lng
  → query toàn tỉnh
```

```java
// LocationResolverService.resolve(state)
if (state.getLocationType() == null) return; // locationType null → query toàn tỉnh, không set gì thêm
switch (state.getLocationType()) {
    case "ward" -> {
        var wards = wardRepository.searchByNameInProvince(
            state.getLocalKeyword(), state.getProvinceCode());
        // empty = ph\u01b0\u1eddng kh\u00f4ng t\u00ecm th\u1ea5y → null = query to\u00e0n t\u1ec9nh (fallback)
        state.setResolvedWardCodes(wards.isEmpty() ? null : wards.stream().map(Ward::getCode).toList());
    }
    case "landmark", "street" -> {
        GeocodingResult result = geocodingService.geocode(state.getPlaceName(), provinceName);
        if (result != null && result.isUsable()) {
            state.setCenterLat(result.lat());
            state.setCenterLng(result.lng());
            // ROOFTOP/RANGE → 3km, GEOMETRIC_CENTER → 5km
            // confidence ≥ 8 → 3km (building/POI), 5-7 → 5km (street/area)
            state.setSearchRadiusKm(result.confidence() >= 8 ? 3.0 : 5.0);
        } else {
            // fallback: LIKE search ward từ placeName
            var wards = wardRepository.searchByNameInProvince(
                state.getPlaceName(), state.getProvinceCode());
            // empty = không tìm thấy phường → null = query toàn tỉnh (fallback, giống case "ward")
            state.setResolvedWardCodes(wards.isEmpty() ? null : wards.stream().map(Ward::getCode).toList());
        }
    }
    case "province_only" -> { /* query toàn tỉnh, không set gì thêm */ }
}
```

---

## 8. State Machine — 2 Phase

### Phase 1: SLOT_FILLING

```
INITIAL
  ↓ user gửi message
EXTRACTING  (1 lần gọi Gemini — Prompt 1)                     ← đây là lần gọi DUY NHẤT
  ↓
  ├── intent = off_topic
  │     → ChatController trả String template cứng ngay, KHÔNG gọi Gemini thêm
  │     → EXTRACTING (chờ user nhập lại)
  ├── intent = ambiguous
  │     → dùng geminiResult.clarificationNeeded() đã có sẵn trong response trước
  │     → ChatController đọc field này ra, gửi thẳng cho user, KHÔNG gọi Gemini thêm
  │     → EXTRACTING (chờ user làm rõ)
  ├── required fields thiếu (missingRequired không rỗng)
  │     → ChatController tra bảng MAP_QUESTION cứng theo field thiếu đầu tiên
  │     → Trả câu hỏi template cho user, KHÔNG gọi Gemini thêm
  │     → EXTRACTING (chờ user bổ sung)
  └── required fields đủ + provinceCode hợp lệ (sau validate)
        → LocationResolverService.resolve() — Java thuần, 0 lần gọi Gemini
        → QUERYING → SHOWING_RESULTS
```

**Template cứng cho missingRequired (MAP_QUESTION):**

```java
// Trong ChatController — không gọi Gemini, chỉ tra map
private static final Map<String, String> MISSING_FIELD_QUESTION = Map.of(
    "location", "Ạ, bạn muốn tìm phòng ở tỉnh/thành nào ạ? (VD: Hồ Chí Minh, Hà Nội)",
    "budget",   "Ạ, ngân sách dự kiến của bạn khoảng bao nhiêu một tháng ạ?"
);

// Thứ tự ưu tiên hỏi: location trước, budget sau
private String pickFirstQuestion(List<String> missingRequired) {
    if (missingRequired.contains("location")) return MISSING_FIELD_QUESTION.get("location");
    if (missingRequired.contains("budget"))   return MISSING_FIELD_QUESTION.get("budget");
    return null;
}
```

**Template cứng cho off_topic:**

```java
private static final String OFF_TOPIC_REPLY =
    "Ạ, mình chỉ hỗ trợ tìm phòng trọ thôi ạ. " +
    "Bạn có thể mô tả nhu cầu như: ngân sách, khu vực muốn ở, " +
    "tiện ích cần có. Mình sẽ tìm giúp bạn!";
```

**Thứ tự hỏi khi thiếu required fields:**

1. `provinceCode` / location (hỏi trước)
2. `budget` (hỏi sau)

**Ward resolution — Java thuần, không tốn token:**

> `LocationResolverService` quyết định strategy dựa trên `locationType`.
> Ward DB là tầng đầu tiên (miễn phí). Geocoding API chỉ gọi khi địa điểm cụ thể.

```
Case 1: locationType = "ward"
  provinceCode = "79", localKeyword = "Long Thạnh Mỹ"
        ↓ LIKE search Ward DB
  wardCodes = ["26875"]  (Phường Long Thạnh Mỹ)
        ↓
  Query DB với wardCodes → bao phủ khu vực

Case 2: locationType = "landmark"
  placeName = "Đại học FPT Hồ Chí Minh"
        ↓ OpenCage Geocoding API
  { lat: 10.8414, lng: 106.8101, confidence: 9 }  ← kết quả thực tế từ API
        ↓
  confidence=9 ≥ 8 → radius=3km
  centerLat=10.8414, centerLng=106.8101, radius=3km
        ↓
  Bounding box → Haversine filter trong Java

Case 3: Geocoding fail
        ↓ fallback
  LIKE search ward từ placeName → tiếp tục như Case 1

Case 4: locationType = "province_only"
  provinceCode = "01"  → query toàn Hà Nội, không filter ward
```

### Phase 2: SHOWING_RESULTS

```
SHOWING_RESULTS
  ↓ user gửi message
  ├── action = refine      → cập nhật state.params (cùng khu vực) → QUERYING → SHOWING_RESULTS
  ├── action = new_search  → reset toàn bộ state → SLOT_FILLING (Phase 1)
  ├── action = question    → Gemini đọc cachedResults → reply → SHOWING_RESULTS
  ├── action = interest    → trả postSlug của bất động sản được chỉ định → SHOWING_RESULTS
  └── action = exit        → clear session → INITIAL
```

---

## 9. Kịch Bản (Scenarios)

### Kịch bản 1 — User mô tả đầy đủ ngay lần 1

```
User: "Sinh viên, 5tr, gần ĐH FPT HCM, cần điều hòa"
→ Gemini extract: đủ required fields
→ Query DB ngay
→ Trả danh sách phòng
Số lần gọi Gemini: 1 (extract) + 0 (template cứng cho kết quả)
```

### Kịch bản 2 — User mô tả thiếu thông tin

```
User: "tìm phòng trọ cho tôi"
→ Gemini extract: intent = find_room, missingRequired = ["location", "budget"]
→ ChatController tra MAP_QUESTION → hỏi location trước (không gọi Gemini lần 2)
→ User: "HCM, budget 5tr" → Gemini extract đủ required fields → query DB
```

### Kịch bản 3 — Ambiguous location

```
User: "tôi học FPT, budget 5tr"
→ Gemini extract: budget=5tr, provinceCode=null (FPT có ở nhiều tỉnh → ambiguous)
→ intent = ambiguous, clarificationNeeded = "FPT ở thành phố nào ạ?"
→ User: "HCM"
→ Gemini extract: provinceCode="79", locationType="landmark", placeName="Đại học FPT Hồ Chí Minh"
→ LocationResolverService: locationType=landmark → gọi OpenCage
→ OpenCage: { lat: 10.8414, lng: 106.8101, confidence: 9 } → radius=3km
→ state.centerLat=10.8414, centerLng=106.8101, radius=3km
→ Query DB: bounding box → Haversine filter
```

### Kịch bản 4 — Follow-up về kết quả (không query lại)

```
User (sau khi có kết quả): "cái thứ 2 có gần chợ không?"
→ action = question
→ Gemini đọc cachedResults → trả lời
→ Không gọi DB
```

### Kịch bản 5 — User muốn refine (query lại)

```
User: "cho tôi xem thêm loại có điều hòa"
→ action = refine, addAmenityIds: [6]
→ Cập nhật state.amenityIds
→ Query lại DB với params mới
→ Trả danh sách mới
```

### Kịch bản 5b — Context Switch (Đổi khu vực đột ngột ở Phase 2)

```
User (đang xem kết quả khu FPT HCM): "À thôi, tìm phòng ở Bình Thạnh đi"
→ action = new_search  (Gemini nhận ra đổi hẳn khu vực/tỉnh)
→ Server reset toàn bộ ChatSessionState (giữ guestId)
→ Chuyển về Phase 1: SLOT_FILLING
→ Xử lý cùng message đó như một message mới hoàn toàn

Khác gì refine?
  refine     = giữ khu vực, chỉ điều chỉnh filter (budget, amenity, category)
  new_search = đổi hẳn địa điểm → reset hoàn toàn → Phase 1
```

### Kịch bản 6 — Không có kết quả → Relaxed search

```
Query strict (budget ≤ 5tr + amenity + ward) → 0 kết quả
→ Relaxed lần 1: budget ≤ 5.5tr (nới 10%) + amenity + ward → x kết quả
→ Relaxed lần 2: budget ≤ 5tr + bỏ amenity filter + ward → y kết quả
→ Relaxed lần 3: bỏ tất cả, lấy rẻ nhất trong province → z kết quả
→ Thông báo rõ: "Không tìm thấy phòng đúng yêu cầu, nhưng có các lựa chọn gần nhất..."
```

### Kịch bản 7 — User bày tỏ thích 1 bất động sản

```
User: "tôi thích phòng số 1"
→ Gemini: { action: "interest", targetPropertyIndex: 1 }
→ Server: cached[0].postSlug  ← KHÔNG tin Gemini về ID
→ Trả về postSlug để frontend highlight card + hiện nút "Xem chi tiết" nổi bật
```

### Kịch bản 8 — Yêu cầu bất khả thi

```
User: "budget 500k, có hồ bơi, điều hòa, gần trung tâm"
→ Tất cả relaxed search đều 0 kết quả
→ Bot: "Với ngân sách 500k hiện tại chưa có phòng phù hợp trong hệ thống,
        bạn có thể điều chỉnh ngân sách lên không?"
→ action = refine được khởi tạo bởi bot (không phải user)
```

---

## 10. Constrained Extraction — Tránh Hallucination

Trước khi gọi Gemini, query tất cả reference data và đưa vào prompt:

```java
// Song song (tất cả đều nhỏ, đủ điều kiện đưa vào prompt)
List<Amenity> amenities = amenityRepository.findAll();           // ~10 items
List<Surrounding> surroundings = surroundingRepository.findAll(); // ~7 items
List<TargetTenant> targets = targetTenantsRepository.findAll();   // ~4 items
List<Category> categories = categoryRepository.findAll();         // 4 items
List<Province> provinces = provinceRepository.findAll();
// VD: [{"code":"79","name":"Thành phố Hồ Chí Minh"},{"code":"01","name":"Thành phố Hà Nội"},...]
// KHÔNG gửi Ward lên — quá nhiều (hàng nghìn records)
```

Prompt gửi lên:

```
LOẠI BĐS:      [{"id":1,"name":"Nhà trọ"},{"id":2,"name":"Nhà nguyên căn"},
                {"id":3,"name":"Ký túc xá"},{"id":4,"name":"Căn hộ"}]
TIỆN ÍCH:      [{"id":1,"name":"Wifi"},{"id":6,"name":"Điều hòa"},...]
XUNG QUANH:    [{"id":4,"name":"Trường học"},{"id":1,"name":"Chợ"},...]
ĐỐI TƯỢNG:     [{"id":1,"name":"Sinh viên"},{"id":2,"name":"Người đi làm"},...]
TỈNH/THÀNH:    [{"code":"79","name":"Thành phố Hồ Chí Minh"},{"code":"01","name":"Thành phố Hà Nội"},...]
```

**Tại sao gửi Province list thay vì để Gemini tự biết:**

- Tránh Gemini dùng tên tỉnh cũ trước sáp nhập (vd: "Thủ Đức" thay vì "Hồ Chí Minh")
- DB dùng tên nào thì prompt gửi đúng tên đó → luôn match
- Province chỉ ~34-63 items → token không đáng kể (~80 tokens)

Gemini chỉ chọn từ danh sách trên → trả về ID, không tự bịa.

**Validate sau khi nhận về từ Gemini — bắt buộc trước khi query DB:**

```java
// 1. provinceCode: LLM có thể hallucinate code không tồn tại
Set<String> validProvinceCodes = provinceRepository.findAll()
    .stream().map(Province::getCode).collect(toSet());
if (!validProvinceCodes.contains(geminiResult.provinceCode())) {
    // hỏi lại user thay vì query DB với code rác
    throw new InvalidLocationException("Province code không hợp lệ: " + geminiResult.provinceCode());
}

// 2. amenityIds, surroundingIds, categoryId: xác nhận tồn tại trong DB
// Gemini có thể trả về null thay vì [] khi không có → dùng emptyList() để tránh IllegalArgumentException
List<Long> rawAmenityIds     = geminiResult.amenityIds()     != null ? geminiResult.amenityIds()     : List.of();
List<Long> rawSurroundingIds = geminiResult.surroundingIds() != null ? geminiResult.surroundingIds() : List.of();
Set<Long> validAmenityIds = rawAmenityIds.isEmpty() ? Set.of() :
    amenityRepository.findAllById(rawAmenityIds).stream().map(Amenity::getId).collect(toSet());
Set<Long> validSurroundingIds = rawSurroundingIds.isEmpty() ? Set.of() :
    surroundingRepository.findAllById(rawSurroundingIds).stream().map(Surrounding::getId).collect(toSet());
// categoryId: kiểm tra riêng
boolean validCategory = geminiResult.categoryId() == null ||
    categoryRepository.existsById(geminiResult.categoryId());
// Bỏ qua ID không tồn tại, không throw exception — chỉ filter ra
```

> **Lý do kiểm tra provinceCode riêng:** Khác với amenityIds (bỏ qua cũng không sao), một
> `provinceCode` sai sẽ khiến toàn bộ query trả về rỗng hoặc sai tỉnh — phải chặn trước.

---

## 11. Prompt Templates

### Prompt 1 — Phase 1: Extract Params (bao gồm cả resolve location)

> **Lưu ý:** Gemini extract `locationType` + `placeName`/`localKeyword` ngay trong Phase 1 — không cần API call
> riêng để resolve location. Province list được gửi kèm để Gemini chọn đúng tên sau sáp nhập.

````
Bạn là trợ lý tìm phòng trọ tại Việt Nam.

=== DỮ LIỆU HỆ THỐNG ===
LOẠI BĐS:   {categoriesJson}
TIỆN ÍCH:   {amenitiesJson}
XUNG QUANH: {surroundingsJson}
ĐỐI TƯỢNG:  {targetTenantsJson}
TỈNH/THÀNH: {provincesJson}  ← dùng tên địa chỉ hành chính hiện hành của Việt Nam
                               ← VD: [{"code":"79","name":"Thành phố Hồ Chí Minh"},{"code":"01","name":"Thành phố Hà Nội"},...]

=== THÔNG TIN ĐÃ BIẾT ===
Ngân sách:   {budget hoặc "chưa biết"}
Tỉnh/Thành:  {provinceCode hoặc "chưa biết"}
Khu vực cụ thể: {placeName hoặc localKeyword hoặc "chưa biết"}  ← placeName khi locationType=landmark/street; localKeyword khi locationType=ward
Loại BĐS:   {categoryId hoặc "chưa biết"}
Tiện ích:   {amenityIds hoặc "chưa biết"}

=== HỘI THOẠI GẦN ĐÂY (4 turn) ===
{recentHistory}

=== TIN NHẮN MỚI ===
User: {userMessage}

Phân tích và trả về JSON only, không markdown, không giải thích:
```json
{
  "intent": "find_room | off_topic | ambiguous",
  "extracted": {
    "budget": <số VNĐ hoặc null>,
    "provinceCode": "<code từ danh sách TỈNH/THÀNH ở trên, hoặc null>",
    "locationType": "ward | landmark | street | province_only",
    "localKeyword": "<tên phường có dấu — chỉ điền khi locationType=ward, null nếu không>",
    "placeName": "<tên địa điểm đầy đủ — chỉ điền khi locationType=landmark/street, null nếu không>",
    "categoryId": <id hoặc null>,
    "amenityIds": [<id>],
    "surroundingIds": [<id>],
    "targetTenantId": <id hoặc null>
  },
  "missingRequired": ["budget" | "location"],
  "clarificationNeeded": "<câu hỏi làm rõ nếu ambiguous, null nếu không cần>"
}
```

Lưu ý về locationType:

- "ward": user nhắc đến tên phường/xã/khu vực hành chính
  → localKeyword = tên phường tiếng Việt có dấu (không cần tiền tố "Phường")
  → VD: "khu Bình Thạnh" → locationType="ward", localKeyword="Bình Thạnh"
- "landmark": địa điểm cụ thể (trường ĐH, bệnh viện, trung tâm thương mại, KCN...)
  → placeName = tên đầy đủ để Geocoding API tìm được
  → VD: "gần ĐH FPT HCM" → locationType="landmark", placeName="Đại học FPT Hồ Chí Minh"
- "street": tên đường hoặc địa chỉ cụ thể
  → placeName = tên đường + số nhà nếu có
  → VD: "đường Nguyễn Văn Linh Q7" → locationType="street", placeName="Đường Nguyễn Văn Linh, Quận 7"
- "province_only": user chỉ nói tỉnh/thành phố, không có khu vực cụ thể
  → localKeyword = null, placeName = null
  → VD: "Hà Nội" → locationType="province_only"
- provinceCode phải lấy từ danh sách TỈNH/THÀNH, không tự bịa
- Việt Nam hiện chỉ có 2 cấp: Tỉnh/Thành phố và Phường/Xã — không còn quận/huyện
````

### Prompt 2 — Phase 2: Detect Action + Generate Reply

````
Bạn là trợ lý tìm phòng trọ.

=== THÔNG TIN TÌM KIẾM HIỆN TẠI ===
Ngân sách: {budget} | Tỉnh/Thành: {provinceName} | Khu vực: {placeName hoặc localKeyword} | Tiện ích: {amenityNames}

=== KẾT QUẢ HIỆN TẠI ===
{cachedResultsJson} ← chỉ gồm: displayIndex, price, area, wardName, amenityNames

=== HỘI THOẠI GẦN ĐÂY ===
{recentHistory}

User: {userMessage}

Trả về JSON only, không markdown, không giải thích:

```json
{
  "action": "refine | new_search | question | interest | exit",
  "refinement": {
    "newBudget": null,
    "addAmenityIds": [],
    "removeAmenityIds": [],
    "newCategoryId": null
  },
  "targetPropertyIndex": <số thứ tự 1-based nếu action=interest, null nếu không>,
  "reply": "<câu trả lời tự nhiên tiếng Việt>"
}
```

Lưu ý:

- action=refine: user điều chỉnh filter trên cùng khu vực (budget, amenity, category)
- action=new_search: user đổi hẳn khu vực/tỉnh — server sẽ reset state và bắt đầu lại Phase 1
- action=question: reply dựa hoàn toàn trên KẾT QUẢ HIỆN TẠI
- action=interest: targetPropertyIndex là số thứ tự trong danh sách (1, 2, 3...)
- action=exit: user không muốn tìm nữa

````

---

## 12. Ranking & Scoring Kết Quả

Dùng **scoring system** thay vì chained comparator để xử lý tie-breaking tự nhiên:

```java
// centerLat/Lng/searchRadiusKm: null khi chỉ dùng ward-based, non-null khi dùng Geocoding
private double score(PropertySummaryForChat r, double budget, List<Long> requestedAmenityIds,
                     Double centerLat, Double centerLng, Double searchRadiusKm) {
    double score = 0;

    // 1. Boost (30đ) — owner đã trả tiền ưu tiên hiển thị
    if (r.isBoosted()) score += 30;

    // 2. Amenity match rate (0~25đ) — đáp ứng được nhiều nhu cầu user
    if (requestedAmenityIds != null && !requestedAmenityIds.isEmpty()) {
        double matchRate = (double) r.amenityMatchCount() / requestedAmenityIds.size();
        score += matchRate * 25;
    }

    // 3. Gần budget (0~20đ) — không phải rẻ nhất, mà gần nhất với số user đưa ra
    // VD: user nói 5tr → phòng 4.8tr > phòng 2tr
    double budgetProximity = 1 - Math.abs(r.price() - budget) / budget;
    score += Math.max(0, budgetProximity) * 20;

    // 4. Location score (0~10đ) — distance-based nếu có tọa độ, ward-based nếu không
    if (centerLat != null && centerLng != null && searchRadiusKm != null && r.latitude() != null) {
        double distKm = haversine(centerLat, centerLng, r.latitude(), r.longitude());
        score += Math.max(0, (1 - distKm / searchRadiusKm)) * 10;
        // 0km → 10đ | nửa bán kính → 5đ | ranh giới → 0đ
    } else if (r.isWardMatch()) {
        score += 10; // fallback: ward-based
    }

    // 5. Post sống lâu (0~5đ) — bài đăng còn hạn lâu, ổn định hơn
    long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), r.postExpiredAt());
    score += Math.min(daysLeft, 30) * (5.0 / 30);

    return score; // max = 90đ
}
```

**Sort + tie-breaker:**

```java
results.sort(Comparator
    .comparingDouble((PropertySummaryForChat r) ->
        -score(r, budget, amenityIds, state.getCenterLat(), state.getCenterLng(), state.getSearchRadiusKm()))
    .thenComparing(PropertySummaryForChat::price,
        Comparator.nullsLast(Comparator.naturalOrder()))  // cùng điểm → rẻ hơn thắng; null-safe vì Double là boxed
    .thenComparing(PropertySummaryForChat::postId,
        Comparator.nullsLast(Comparator.naturalOrder()))  // cùng giá → đăng trước thắng; null-safe vì Long là boxed
);
return results.stream().limit(maxResults).toList();   // maxResults = 5 (config)
```

**Trọng số có thể tune trong config:**

```properties
chat.ranking.weight.boost=30
chat.ranking.weight.amenity=25
chat.ranking.weight.budget-proximity=20
chat.ranking.weight.ward-match=10
chat.ranking.weight.post-longevity=5
```

---

## 13. DB Queries Cần Thêm

### `PostRepository`

```java
// Query chính: filter tại DB theo budget + wardCodes + amenityIds
// GROUP BY p đảm bảo unique (JPQL phải GROUP BY entity, không phải p.id) — không dùng SELECT DISTINCT cùng lúc
@Query("SELECT p FROM Post p " +
       "JOIN p.property prop " +
       "JOIN prop.rooms r " +
       "LEFT JOIN prop.amenities a " +
       "WHERE p.status = 'ACTIVE' AND p.postExpiredAt > :now " +
       "AND r.status = 'AVAILABLE' " +
       "AND (:maxPrice IS NULL OR r.price <= :maxPrice) " +
       "AND (:wardCodes IS NULL OR prop.ward.code IN :wardCodes) " +
       "AND (:categoryId IS NULL OR prop.category.id = :categoryId) " +
       "AND (:amenityIds IS NULL OR a.id IN :amenityIds) " +
       "GROUP BY p " +
       "HAVING (:amenityIds IS NULL OR COUNT(DISTINCT a.id) >= :amenityMatchCount) " +
       "ORDER BY CASE WHEN p.boostExpiredAt > :now THEN 0 ELSE 1 END, p.postExpiredAt DESC")
List<Post> findRecommendedPosts(
    @Param("now") LocalDateTime now,
    @Param("maxPrice") Double maxPrice,
    @Param("wardCodes") List<String> wardCodes,
    @Param("categoryId") Long categoryId,
    @Param("amenityIds") List<Long> amenityIds,
    @Param("amenityMatchCount") long amenityMatchCount
    // ⚠️ amenityMatchCount: truyền amenityIds.size() khi amenityIds != null, truyền 0L khi amenityIds == null
    //   (tránh NPE: amenityIds.size() khi null; 0L an toàn vì HAVING clause bỏ qua khi amenityIds IS NULL)
);

// Relaxed: bỏ ward filter, nới budget
// SELECT DISTINCT p vì JOIN rooms có thể sinh duplicate; ORDER BY postExpiredAt (r.price không chọn được sau DISTINCT)
@Query("SELECT DISTINCT p FROM Post p JOIN p.property prop JOIN prop.rooms r " +
       "WHERE p.status = 'ACTIVE' AND p.postExpiredAt > :now " +
       "AND r.status = 'AVAILABLE' AND r.price <= :maxPrice " +
       "ORDER BY p.postExpiredAt DESC")
List<Post> findRelaxedRecommendedPosts(
    @Param("now") LocalDateTime now,
    @Param("maxPrice") Double maxPrice
);
```

### `WardRepository`

```java
// Tìm phường/xã theo tên trong đúng tỉnh/thành phố
// Việt Nam 2 cấp: Province → Ward (không còn quận/huyện)
@Query("SELECT w FROM Ward w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
       "AND w.province.code = :provinceCode")
List<Ward> searchByNameInProvince(@Param("keyword") String keyword,
                                   @Param("provinceCode") String provinceCode);

// Fallback: tất cả phường/xã trong tỉnh/thành khi localKeyword = null (đã có sẵn)
// List<Ward> findByProvince_Code(String provinceCode);
```

---

## 14. PropertySummaryForChat DTO

> **Ý quan trọng khi implement:** Record chỉ chứa kiểu dữ liệu thuần túy (primitives, String, List\<String\>).
> Tuyệt đối không lưu Hibernate entity/proxy vào `cachedResults` trong HttpSession
> — sẽ gây `LazyInitializationException` hoặc memory leak.

Chỉ gửi các field cần thiết lên Gemini để tiết kiệm token:

```java
public record PropertySummaryForChat(
    int displayIndex,         // 1, 2, 3... (để Gemini dùng)
    Long postId,              // để sort tie-breaker
    Double price,
    Double area,
    Integer maxOccupancy,
    String wardName,
    String provinceName,
    String postTitle,
    String postSlug,           // để redirect
    List<String> amenityNames,
    List<String> surroundingNames,
    String categoryName,
    // --- Fields cho Haversine scoring (không gửi lên Gemini) ---
    Double latitude,          // tọa độ property, null nếu chưa có
    Double longitude,
    boolean isBoosted,
    int amenityMatchCount,
    boolean isWardMatch,
    LocalDateTime postExpiredAt
) {}
```

---

## 15. Token Management

**Sliding window — chỉ giữ 4 turn gần nhất:**

```
Cấu trúc prompt mỗi lần gọi:       Token ước tính
  System instruction                 ~50   (cố định)
  Reference data (amenities...)      ~100  (cố định, chỉ phase 1)
  Province list (~34-63 items)       ~80   (cố định, chỉ phase 1)
  Known params                       ~30   (cố định)
  4 turns history                    ~80   (cố định, không tăng theo thời gian)
  New user message                   ~20
  ──────────────────────────────────────
  Tổng phase 1                       ~360  (ổn định)
  Tổng phase 2                       ~280  (không có reference data & province)
```

**Tại sao province list không tốn nhiều token:**

- ~34-63 tỉnh/thành × ~5 tokens/item ≈ 80 tokens
- Chỉ gửi ở phase 1, không gửi ở phase 2
- Đổi lại: gộp location extraction vào Phase 1, không cần thêm API call riêng cho resolve location

**Tại sao không mất thông tin dù xóa history:**

- Params đã extract (budget, location...) được lưu riêng trong `ChatSessionState`
- Mỗi prompt đều có section "THÔNG TIN ĐÃ BIẾT" inject từ state
- History chỉ cần để hiểu ngữ cảnh câu hỏi ngay trước ("cái nào?", "ở đó có không?")

---

## 16. Số Lần Gọi Gemini Per Message

| Tình huống                  | Lần gọi                                 |
| --------------------------- | --------------------------------------- |
| User mô tả đủ ngay lần 1    | 1 (extract) + 0 (kết quả dùng template) |
| User cần hỏi thêm N lần     | N lần (mỗi lần extract + hỏi lại)       |
| Follow-up / interest / exit | 1 (phase 2 detect + reply)              |
| Off-topic (hướng dẫn cứng)  | 1 (detect intent, template cứng)        |

**Nguyên tắc: luôn đúng 1 lần gọi Gemini per message.**

---

## 17. Security & Rate Limiting

- Endpoint `/api/chat/message` để public (không cần login)
- Rate limit per `guestId`: max 20 request/10 phút (đếm trong HttpSession)
- Validate tất cả IDs trả về từ Gemini trước khi query DB
- Không để Gemini tự sinh `propertyId`, `postId`, `wardCode` — chỉ cho phép index/text

---

## 18. application.properties

```properties
# Gemini API
gemini.api.key=${GEMINI_API_KEY}
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
gemini.timeout.connect=5000
gemini.timeout.read=30000

# OpenCage Geocoding API (free 2500 req/day)
geocode.api.key=${OPENCAGE_API_KEY}
geocode.api.url=https://api.opencagedata.com/geocode/v1/json
geocode.timeout.connect=3000
geocode.timeout.read=5000
geocode.radius.confident=3.0
geocode.radius.approximate=5.0
geocode.confidence.min=5

# Chat recommendation config
chat.recommendation.max-results=5
chat.recommendation.relaxed-budget-multiplier=1.2
chat.recommendation.max-history-turns=4
chat.session.timeout-minutes=30
chat.rate-limit.max-requests=20
chat.rate-limit.window-minutes=10

# Ranking weights
chat.ranking.weight.boost=30
chat.ranking.weight.amenity=25
chat.ranking.weight.budget-proximity=20
chat.ranking.weight.location=10
chat.ranking.weight.post-longevity=5
```

---

## 19. Tích Hợp OpenCage Geocoding API

> Áp dụng khi `locationType = landmark | street`.
> Ward-based vẫn là default cho `locationType = ward | province_only`.
> Free tier: **2500 req/ngày**, không cần billing, không cần credit card.

### Schema thêm vào `Property`

```java
@Column(nullable = true)
private Double latitude;

@Column(nullable = true)
private Double longitude;
```

### Response OpenCage — chỉ dùng các field này

```json
// GET https://api.opencagedata.com/geocode/v1/json
//   ?q=Đại+học+FPT+Hồ+Chí+Minh,+Thành+phố+Hồ+Chí+Minh,+Việt+Nam
//   &key={API_KEY}
//   &language=vi
//   &countrycode=vn
//   &limit=1           ← chỉ lấy kết quả tốt nhất
//   &no_annotations=1  ← bỏ timezone/sun/currency để response nhỏ hơn

// Chỉ extract:
{
  "results": [
    {
      "geometry": { "lat": 10.8414168, "lng": 106.8100745 }, // ← dùng
      "confidence": 9, // ← dùng (1-10)
      "components": {
        "suburb": "Phường Long Bình", // ← dùng nếu có (ward name)
        "_type": "university" // ← kiểm tra không phải bus_stop
      }
    }
  ]
}
// Bỏ qua: annotations, bounds, formatted, OSM data...
```

### `GeocodingResult` record

```java
public record GeocodingResult(
    double lat,
    double lng,
    int confidence,      // 1-10, càng cao càng chính xác
    String suburb        // tên phường từ API, có thể null
) {
    public boolean isUsable() { return confidence >= 5; }
    public double suggestedRadius() { return confidence >= 8 ? 3.0 : 5.0; }
}
```

### Bán kính theo confidence

| Confidence | Ý nghĩa                                    | Bán kính      |
| ---------- | ------------------------------------------ | ------------- |
| **8-10**   | Building / POI level (tòa nhà, trường học) | **3km**       |
| **5-7**    | Street / neighborhood level                | **5km**       |
| **< 5**    | Quá mờ, không đáng tin                     | fallback ward |

### Flow `GeocodingService`

```java
public GeocodingResult geocode(String placeName, String provinceName) {
    // Query: thêm province vào để disambiguate
    // VD: "Đại học FPT, Thành phố Hồ Chí Minh, Việt Nam"
    // → OpenCage trả campus HCM, không phải HN hay Đà Nẵng
    String query = placeName + ", " + provinceName + ", Việt Nam";

    // GET https://api.opencagedata.com/geocode/v1/json
    //   ?q={encodedQuery}&key={key}&language=vi&countrycode=vn&limit=1&no_annotations=1

    // Parse:
    // status.code == 200 && results.length > 0?
    // → lấy results[0]
    // → confidence < 5 hoặc _type == "bus_stop" → trả null (fallback)
    // → trả GeocodingResult(lat, lng, confidence, suburb)

    // Trả null nếu: status != 200, results rỗng, confidence < 5
}
```

### Query khi có tọa độ — 2 bước

```java
// Bước 1: Bounding box tại DB (nhanh, dùng index)
double deltaLat = radiusKm / 111.0;
double deltaLng = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));
// WHERE prop.latitude  BETWEEN (centerLat - deltaLat) AND (centerLat + deltaLat)
// AND   prop.longitude BETWEEN (centerLng - deltaLng) AND (centerLng + deltaLng)

// Bước 2: Haversine filter trong Java (cắt góc vuông → hình tròn)
properties.stream()
    .filter(p -> p.getLatitude() != null)
    .filter(p -> haversine(centerLat, centerLng, p.getLatitude(), p.getLongitude()) <= radiusKm)
    .collect(toList());
```

### Công thức Haversine

```java
private double haversine(double lat1, double lng1, double lat2, double lng2) {
    final double R = 6371.0;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return R * 2 * Math.asin(Math.sqrt(a)); // km
}
```

### Relaxed search khi có tọa độ

```
Strict:   radius = 3km  → x kết quả
Relaxed1: radius = 5km  → y kết quả
Relaxed2: radius = 10km → z kết quả
Relaxed3: bỏ radius, lấy rẻ nhất toàn tỉnh
```

Tự nhiên hơn ward-based fallback (bỏ ward filter rất ngắt quãng).

### Scoring khi có tọa độ

> Logic distance score đã được tích hợp trực tiếp vào `score()` ở **Section 12** (bước 4).
> Khi `centerLat/Lng != null` → dùng Haversine liên tục (0~10đ). Khi null → fallback ward match (10đ).

---

## 20. Edge Cases & Implementation Notes

### A. Context Switch (new_search)

Khi Gemini trả về `action = new_search`, server cần reset toàn bộ `ChatSessionState` nhưng giữ `guestId`, rồi xử lý lại cùng message đó như Phase 1:

```java
// ChatController.java — Phase 2 handler
if ("new_search".equals(action.action())) {
    String guestId = state.getGuestId();
    session.removeAttribute("chatState");          // xóa state cũ
    ChatSessionState fresh = new ChatSessionState(guestId);
    session.setAttribute("chatState", fresh);
    return handlePhase1(fresh, userMessage, session); // tái dùng Phase 1 logic
}
```

### B. Session Serialization Warning

`HttpSession` có thể bị serialize khi Tomcat restart hoặc cluster. `ChatSessionState` phải implement `Serializable`. Các field phải là kiểu dữ liệu thuần (`List<PropertySummaryForChat>`, primitives, `String`) — **không được chứa Hibernate entity/proxy**.

```java
// ĐÚNG ✅
List<PropertySummaryForChat> cachedResults = posts.stream()
    .map(p -> new PropertySummaryForChat(p.getId(), p.getPrice(), ...))
    .collect(Collectors.toList());

// SAI ❌ — lưu entity vào session: sẽ bị LazyInitializationException sau khi deserialize
List<Post> cachedResults = postRepository.findRecommendedPosts(...);
```

---

## 21. Checklist Implement

### Layer DB / Repository

- [ ] Thêm `latitude`, `longitude` vào `Property` entity + migration
- [ ] Thêm `findRecommendedPosts()` vào `PostRepository`
- [ ] Thêm `findRelaxedRecommendedPosts()` vào `PostRepository`
- [ ] Thêm bounding box query (lat/lng BETWEEN) vào `PostRepository`
- [ ] Thêm `searchByNameInProvince()` vào `WardRepository`

### Layer Service

- [ ] Tạo `GeminiService` (gọi API, build prompt, parse JSON response)
- [ ] Tạo `LocationResolverService` (phân loại locationType → ward search hoặc Geocoding)
- [ ] Tạo `GeocodingService` (gọi OpenCage API, parse confidence/suburb, fallback)
- [ ] Tạo `PropertyRecommendationService` (query DB, relaxed search logic, scoring)
- [ ] Cập nhật scoring: distance score thay ward-match khi có tọa độ
- [ ] Tạo `ChatSessionState` (state machine, sliding window history)

### Layer Controller

- [ ] Tạo `ChatController` với `POST /api/chat/message`
- [ ] Tạo `ChatResponse` DTO

### Config

- [ ] Thêm `GeminiProperties` (`@ConfigurationProperties`)
- [ ] Thêm `GeocodingProperties` (`@ConfigurationProperties`)
- [ ] Thêm Gemini + Geocoding config vào `application.properties`

### Frontend

- [ ] Floating chat button trong `public-main.html`
- [ ] Chat panel (Vanilla JS)
- [ ] `localStorage` manager cho guestId + params
- [ ] Render card kết quả + nút "Xem chi tiết"
- [ ] Handle `action=interest` → highlight card
