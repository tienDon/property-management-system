# Demo Script — AI Chat Recommendation

## Tổng quan hệ thống

```
SLOT_FILLING  →  REFINING (optional)  →  SHOWING_RESULTS
```

| Pha                 | Mô tả                                                                                                                                  |
| ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| **SLOT_FILLING**    | Thu thập 2 thông tin bắt buộc: tỉnh/thành + ngân sách. Gemini phân loại intent: `find_room` / `off_topic` / `ambiguous`.               |
| **REFINING**        | Tùy chọn — thu thập tiêu chí nâng cao (khu vực cụ thể, tiện ích, loại phòng). Gồm 2 sub-step: _collect_ → _confirm_.                   |
| **SHOWING_RESULTS** | Hiển thị kết quả. Gemini phân loại từng tin nhắn tiếp theo thành 5 action: `refine` / `question` / `interest` / `new_search` / `exit`. |

### Điều kiện rẽ nhánh vào REFINING

| Số kết quả tỉnh × ngân sách | Hành vi                                                           |
| --------------------------- | ----------------------------------------------------------------- |
| **≤ 5**                     | Bỏ qua REFINING, truy vấn luôn (REFINING_SKIP)                    |
| **6 – 20**                  | Vào REFINING, hỏi tiêu chí thêm — chip: **"Không cần, tìm ngay"** |
| **> 20**                    | Vào REFINING, hỏi khu vực + tiện ích — chip: **"Tìm ngay luôn"**  |

### Fallback tiers khi không có kết quả

| Tier                  | Điều kiện                                    | Hành động          |
| --------------------- | -------------------------------------------- | ------------------ |
| **Tier 1** (strict)   | Ngân sách + địa chỉ + tiện ích + loại phòng  | Query chính        |
| **Tier 2** (relaxed)  | Ngân sách ×1.2, bỏ filter tiện ích           | Nới budget 20%     |
| **Tier 3** (province) | Tỉnh + ngân sách ×1.2, bỏ hết filter còn lại | Tìm rộng toàn tỉnh |

### Scoring kết quả (tối đa 90 điểm)

| Tiêu chí                            | Điểm |
| ----------------------------------- | ---- |
| Post boost (độ ưu tiên bài đăng)    | 30   |
| Tỷ lệ tiện ích khớp                 | 25   |
| Khoảng cách ngân sách               | 20   |
| Vị trí (Haversine hoặc khớp phường) | 10   |
| Độ tuổi bài đăng                    | 5    |

---

## Kịch bản 1 — Demo chính (REFINING_MANY + tất cả 5 actions)

**Mục tiêu:** Cover toàn bộ luồng chính từ tin nhắn đầu đến khi thoát session.  
**Test data cần có:** HCM, nhà trọ, 2–4 triệu → >20 kết quả; BD, gần Làng ĐH, 1.5 triệu → ≤5 kết quả.

> **Gợi ý presenter:** Mở chat widget ở góc phải trang chủ. Không cần đăng nhập.

---

### Bước 0 — Widget khởi động

Bot hiển thị greeting kèm 4 quick-reply chips:  
`[Căn hộ]` `[Ký túc xá / KTX]` `[Nhà trọ / Phòng trọ]` `[Nhà nguyên căn]`

---

### 🏷️ Phần A — Slot Filling

#### Bước 1 — Off-topic intent

|                 |                                                                                                                                                    |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `thời tiết hôm nay thế nào?`                                                                                                                       |
| **Bot trả lời** | _"Ạ, mình chỉ hỗ trợ tìm phòng trọ thôi ạ. Bạn có thể mô tả nhu cầu như: ngân sách, khu vực muốn ở, tiện ích cần có — mình sẽ tìm giúp bạn ngay!"_ |
| **Điều kiện**   | ✅ `intent = off_topic` → reply tĩnh, giữ nguyên SLOT_FILLING                                                                                      |

---

#### Bước 2 — Ambiguous intent

|                 |                                                                                                                                                                |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `tìm phòng đẹp`                                                                                                                                                |
| **Bot trả lời** | _(Gemini sinh câu hỏi làm rõ, ví dụ:)_ _"Bạn muốn tìm phòng ở khu vực nào, và ngân sách khoảng bao nhiêu ạ?"_ + chips `[Căn hộ][KTX][Nhà trọ][Nhà nguyên căn]` |
| **Điều kiện**   | ✅ `intent = ambiguous` → hỏi làm rõ, ở lại SLOT_FILLING                                                                                                       |

---

#### Bước 3 — Thu thập loại phòng qua chip

|                     |                                                                                                                                                                    |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **User click chip** | `Nhà trọ / Phòng trọ`                                                                                                                                              |
| **Bot trả lời**     | _"Ngân sách dự kiến của bạn khoảng bao nhiêu một tháng ạ?"_ <br> + chips `[Dưới 2 triệu/tháng]` `[2 – 4 triệu/tháng]` `[4 – 7 triệu/tháng]` `[7 – 15 triệu/tháng]` |
| **Điều kiện**       | ✅ Slot `categoryId` = Nhà trọ được lưu vào session. Còn thiếu `budget` + `provinceCode`.                                                                          |

---

#### Bước 4 — Thu thập ngân sách qua chip

|                     |                                                                                                                             |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| **User click chip** | `2 – 4 triệu/tháng`                                                                                                         |
| **Bot trả lời**     | _"Bạn muốn tìm phòng ở tỉnh/thành nào ạ? (ví dụ: Hồ Chí Minh, Hà Nội)"_ <br> + chips `[Hồ Chí Minh]` `[Hà Nội]` `[Đà Nẵng]` |
| **Điều kiện**       | ✅ `budget = 4,000,000`. Còn thiếu `provinceCode`.                                                                          |

---

#### Bước 5 — Điền location → đủ required fields → REFINING_MANY

|                 |                                                                                                                                                                                                                                                                                          |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Hồ Chí Minh`                                                                                                                                                                                                                                                                            |
| **Bot trả lời** | _"Mình tìm thấy khoảng **30+ phòng** ở Hồ Chí Minh trong ngân sách của bạn ạ. Để tìm chính xác hơn, bạn muốn ở khu vực/phường cụ thể nào không? Và bạn cần tiện ích gì (máy lạnh, wifi, giường nệm...) hoặc muốn gần chợ, công viên, trường học không ạ?"_ <br> + chip `[Tìm ngay luôn]` |
| **Điều kiện**   | ✅ `count > 20` → `REFINING_MANY` → phase = REFINING, `refiningConfirmStep = false`                                                                                                                                                                                                      |

---

### 🏷️ Phần B — REFINING Collect + Confirm

#### Bước 6 — REFINING: Thu thập tiêu chí nâng cao

|                 |                                                                                                                                                                                                   |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Gần khu Tăng Nhơn Phú, cần wifi và máy lạnh`                                                                                                                                                     |
| **Bot trả lời** | Summary confirm: _"Mình sẽ tìm **nhà trọ** tại **Hồ Chí Minh**, khu **Tăng Nhơn Phú**, ngân sách **4 triệu/tháng**, có: **Wifi, Điều hòa**. Tìm ngay nhé?"_ <br> + chips `[Tìm ngay]` `[Sửa lại]` |
| **Điều kiện**   | ✅ Gemini extract: `localKeyword = "Tăng Nhơn Phú"`, `amenityIds = [Wifi, Điều hòa]`. `refiningConfirmStep = true`.                                                                               |

---

#### Bước 7 — REFINING Confirm: Nhấn "Sửa lại"

|                     |                                                                                                                        |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **User click chip** | `Sửa lại`                                                                                                              |
| **Bot trả lời**     | _"Bạn muốn thay đổi điều gì? Hãy nói thêm về khu vực, tiện ích hoặc ngân sách ạ."_ <br> + chip `[Không cần, tìm ngay]` |
| **Điều kiện**       | ✅ Nhận diện từ khóa `"sửa"` → `refiningConfirmStep = false` (quay lại collect)                                        |

---

#### Bước 8 — REFINING Collect lần 2 (cập nhật)

|                 |                                                                                                                         |
| --------------- | ----------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Thêm bình nóng lạnh nữa`                                                                                               |
| **Bot trả lời** | Summary confirm mới: _"...có: **Wifi, Điều hòa, Bình nóng lạnh**. Tìm ngay nhé?"_ <br> + chips `[Tìm ngay]` `[Sửa lại]` |
| **Điều kiện**   | ✅ `amenityIds` được merge thêm "Bình nóng lạnh". `refiningConfirmStep = true`.                                         |

---

#### Bước 9 — REFINING Confirm: Nhấn "Tìm ngay" → SHOWING_RESULTS

|                     |                                                                                                                                         |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **User click chip** | `Tìm ngay`                                                                                                                              |
| **Bot trả lời**     | _"Mình tìm được **N phòng** phù hợp tại Hồ Chí Minh ạ. Bạn xem thử nhé!"_ <br> + kết quả dạng thẻ (tên, địa chỉ, giá rẻ nhất, tiện ích) |
| **Điều kiện**       | ✅ Trigger `executeQueryAndReturn()`. Phase = SHOWING_RESULTS. Kết quả được cache vào session.                                          |

---

### 🏷️ Phần C — SHOWING_RESULTS (5 actions)

#### Bước 10 — Action: `question`

|                 |                                                                                                                                    |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Phòng nào có diện tích lớn nhất?`                                                                                                 |
| **Bot trả lời** | _(Gemini trả lời dựa trên cached results, không query DB)_ <br> _"Phòng số [X] – [Tên phòng] có diện tích lớn nhất với [Y] m²..."_ |
| **Điều kiện**   | ✅ `action = question` → không re-query DB, trả lời từ `cachedResults`                                                             |

---

#### Bước 11 — Action: `interest`

|                 |                                                                                                                                                           |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Cho mình xem chi tiết phòng số 2`                                                                                                                        |
| **Bot trả lời** | _"Bạn có thể xem chi tiết phòng này tại đây ạ!"_ + link đến trang post                                                                                    |
| **Điều kiện**   | ✅ `action = interest`, `targetPropertyIndex = 2` → server map sang `cachedResults[1].postSlug` → trả về link an toàn (không dùng ID từ Gemini trực tiếp) |

---

#### Bước 12 — Action: `refine`

|                 |                                                                                                                                  |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Tăng budget lên 5 triệu, không cần điều hòa nữa`                                                                                |
| **Bot trả lời** | _(Re-query với budget 5M, bỏ amenity Điều hòa)_ <br> _"Mình tìm được **M phòng** sau khi cập nhật yêu cầu ạ."_ + kết quả mới     |
| **Điều kiện**   | ✅ `action = refine` → `refinement.newBudget = 5,000,000`, `removeAmenityIds = [id Điều hòa]` → re-query, update `cachedResults` |

---

#### Bước 13 — Action: `new_search` → REFINING_SKIP

|                 |                                                                                                                                                                                                                                                                                                                          |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **User gõ**     | `Tìm ở Bình Dương gần Làng Đại Học, budget 1.5 triệu đi`                                                                                                                                                                                                                                                                 |
| **Bot trả lời** | _(Session reset → re-process cùng tin nhắn qua SLOT_FILLING)_ <br> → Gemini extract: province=BD, placeName="Làng Đại Học", budget=1,500,000 <br> → `count ≤ 5` (BD chỉ có 4 phòng test) → **bỏ qua REFINING** → query trực tiếp <br> _"Mình tìm được **K phòng** phù hợp tại Bình Dương ạ. Bạn xem thử nhé!"_ + kết quả |
| **Điều kiện**   | ✅ `action = new_search` → `state.reset()` → `handleSlotFilling(sameMessage)` → `count ≤ 5` → **REFINING_SKIP**                                                                                                                                                                                                          |

> **Lưu ý:** Chiến lược vị trí ở đây dùng `locationType = landmark`, geocode "Làng Đại Học Đông Hòa Bình Dương" → Haversine radius query thay vì ward filter.

---

#### Bước 14 — Action: `exit`

|                 |                                                                                                   |
| --------------- | ------------------------------------------------------------------------------------------------- |
| **User gõ**     | `Ok được rồi, cảm ơn bạn`                                                                         |
| **Bot trả lời** | _"Cảm ơn bạn đã sử dụng dịch vụ. Chúc bạn tìm được phòng ưng ý ạ!"_                               |
| **Điều kiện**   | ✅ `action = exit` → `session.removeAttribute("chatState")` → widget hiển thị trạng thái kết thúc |

---

### ✅ Checklist điều kiện đã cover — Kịch bản 1

| Điều kiện logic                                        | Bước | Trạng thái |
| ------------------------------------------------------ | ---- | ---------- |
| `intent = off_topic` (tin nhắn không liên quan)        | 1    | ✅         |
| `intent = ambiguous` (thiếu thông tin)                 | 2    | ✅         |
| Slot fill incremental qua chips                        | 3–5  | ✅         |
| `count > 20` → REFINING_MANY                           | 5    | ✅         |
| REFINING collect — extract amenity + location          | 6    | ✅         |
| REFINING confirm → "Sửa lại" → back to collect         | 7    | ✅         |
| REFINING re-collect (merge amenity)                    | 8    | ✅         |
| REFINING confirm → "Tìm ngay" → execute query          | 9    | ✅         |
| `action = question` (trả lời từ cache)                 | 10   | ✅         |
| `action = interest` (link chi tiết, server-side index) | 11   | ✅         |
| `action = refine` (điều chỉnh budget + amenity)        | 12   | ✅         |
| `action = new_search` + `count ≤ 5` → REFINING_SKIP    | 13   | ✅         |
| `action = exit` (xóa session)                          | 14   | ✅         |

---

## Kịch bản 2 — Phụ trợ (REFINING_FEW + Fallback tiers)

**Mục tiêu:** Demo nhánh REFINING_FEW và cơ chế tự động nới lỏng điều kiện tìm kiếm.  
**Test data cần có:** HCM, nhà trọ, dưới 2 triệu → 6–14 kết quả → REFINING_FEW.

---

#### Bước 1 — Single-turn slot fill (một tin nhắn, đủ cả 2 điều kiện)

|                 |                                                                                                                                                                                      |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **User gõ**     | `tìm nhà trọ ở Hồ Chí Minh dưới 2 triệu`                                                                                                                                             |
| **Bot trả lời** | _"Mình tìm thấy **~14 phòng** ở Hồ Chí Minh ạ. Bạn có muốn thêm tiện ích (máy lạnh, wifi...) hoặc muốn gần chợ, công viên, trường học không ạ?"_ <br> + chip `[Không cần, tìm ngay]` |
| **Điều kiện**   | ✅ Một tin nhắn → Gemini extract đủ `budget + provinceCode`. `count 6–20` → **REFINING_FEW**, `refiningConfirmStep = false`                                                          |

---

#### Bước 2 — REFINING_FEW: Skip bằng chip

|                     |                                                                                                                                |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| **User click chip** | `Không cần, tìm ngay`                                                                                                          |
| **Bot trả lời**     | Summary: _"Mình sẽ tìm nhà trọ tại Hồ Chí Minh, ngân sách 2 triệu/tháng. Tìm ngay nhé?"_ <br> + chips `[Tìm ngay]` `[Sửa lại]` |
| **Điều kiện**       | ✅ `isRefiningSkip(message) = true` → bỏ qua Gemini extract, chuyển thẳng sang confirm step                                    |

---

#### Bước 3 — Confirm → SHOWING_RESULTS

|                     |                                                                               |
| ------------------- | ----------------------------------------------------------------------------- |
| **User click chip** | `Tìm ngay`                                                                    |
| **Bot trả lời**     | _"Mình tìm được **N phòng** phù hợp tại Hồ Chí Minh ạ."_ + kết quả            |
| **Điều kiện**       | ✅ Kết quả từ Linh Xuân (KTX 800k–2M) là những phòng giá rẻ trong khu Làng ĐH |

---

#### Bước 4 — Refine với tiêu chí cực kỳ khắt khe → Fallback Tiers

|                 |                                                                                                                                                                                                                             |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **User gõ**     | `thêm điều hòa, wifi, bình nóng lạnh, tủ quần áo, giường nệm, camera` _(yêu cầu nhiều tiện ích cùng lúc)_                                                                                                                   |
| **Bot xử lý**   | **Tier 1**: query strict (budget ≤2M + tất cả amenity) → 0 kết quả <br> **Tier 2**: budget ×1.2 = 2.4M, bỏ amenity filter → tìm theo vị trí rộng hơn <br> **Tier 3** (nếu vẫn còn trống): toàn tỉnh, chỉ lọc ngân sách      |
| **Bot trả lời** | _(Nếu Tier 2/3 có kết quả)_ _"Mình tìm được **M phòng** sau khi điều chỉnh yêu cầu ạ."_ + kết quả <br> _(Nếu không có gì)_ _"Hiện tại chưa tìm thấy phòng phù hợp... Bạn có muốn điều chỉnh ngân sách hoặc khu vực không?"_ |
| **Điều kiện**   | ✅ Fallback Tier 1 → Tier 2 → Tier 3 hoạt động ngầm, user không cần làm thêm gì                                                                                                                                             |

---

#### Bước 5 — Exit

|                 |                                                                     |
| --------------- | ------------------------------------------------------------------- |
| **User gõ**     | `thôi vậy, cảm ơn`                                                  |
| **Bot trả lời** | _"Cảm ơn bạn đã sử dụng dịch vụ. Chúc bạn tìm được phòng ưng ý ạ!"_ |
| **Điều kiện**   | ✅ `action = exit`                                                  |

---

### ✅ Checklist điều kiện đã cover — Kịch bản 2

| Điều kiện logic                                           | Bước | Trạng thái |
| --------------------------------------------------------- | ---- | ---------- |
| Single-turn fill (budget + province trong cùng 1 tin)     | 1    | ✅         |
| `count 6–20` → REFINING_FEW (chip khác với MANY)          | 1    | ✅         |
| Skip collect bằng `isRefiningSkip()`                      | 2    | ✅         |
| REFINING confirm → execute                                | 3    | ✅         |
| Fallback Tier 1 strict → Tier 2 relaxed → Tier 3 province | 4    | ✅         |
| No-result message                                         | 4    | ✅         |

---

## Tổng hợp điều kiện logic có trong codebase

| #   | Điều kiện                                             | Kịch bản      | Bước |
| --- | ----------------------------------------------------- | ------------- | ---- |
| 1   | `intent = off_topic`                                  | KB1           | 1    |
| 2   | `intent = ambiguous` → hỏi làm rõ                     | KB1           | 2    |
| 3   | `intent = find_room` → merge fields                   | KB1           | 3–5  |
| 4   | Slot fill incremental qua nhiều turn                  | KB1           | 3–5  |
| 5   | Single-turn fill đủ cả 2 required fields              | KB2           | 1    |
| 6   | Validate `provinceCode` (bỏ code không hợp lệ)        | _(ngầm)_      | —    |
| 7   | `count ≤ 5` → REFINING_SKIP (query trực tiếp)         | KB1           | 13   |
| 8   | `count 6–20` → REFINING_FEW                           | KB2           | 1    |
| 9   | `count > 20` → REFINING_MANY                          | KB1           | 5    |
| 10  | REFINING collect: Gemini extract optional filters     | KB1           | 6    |
| 11  | REFINING collect: skip bằng `isRefiningSkip()`        | KB2           | 2    |
| 12  | REFINING confirm → "Sửa lại" → quay lại collect       | KB1           | 7    |
| 13  | REFINING confirm → "Tìm ngay" → execute query         | KB1           | 9    |
| 14  | `action = question` (no re-query, trả từ cache)       | KB1           | 10   |
| 15  | `action = interest` (server-side safe index)          | KB1           | 11   |
| 16  | `action = refine` (budget + add/remove amenity)       | KB1           | 12   |
| 17  | `action = new_search` → `state.reset()` → re-process  | KB1           | 13   |
| 18  | `action = exit` → xóa session                         | KB1           | 14   |
| 19  | Validate `amenityIds` từ Gemini (bỏ ID không tồn tại) | _(ngầm)_      | —    |
| 20  | Validate `categoryId` từ Gemini refine                | _(ngầm)_      | —    |
| 21  | Fallback Tier 1 → Tier 2 (budget×1.2, bỏ amenity)     | KB2           | 4    |
| 22  | Fallback Tier 3 (province-wide, cheapest)             | KB2           | 4    |
| 23  | `locationType = landmark` → geocode → Haversine       | KB1           | 13   |
| 24  | `locationType = ward` → ward code filter              | KB1           | 6–9  |
| 25  | Rate limit: ≥20 req / 10 phút → báo lỗi               | _(edge case)_ | —    |
| 26  | Bot lỗi kỹ thuật → graceful error message             | _(edge case)_ | —    |

---

## Lưu ý kỹ thuật khi demo

- **Reset test data:** Nếu DB đã có dữ liệu, `PropertyInitializer` sẽ bỏ qua (`if (!propertyRepository.findAll().isEmpty()) return`). Cần xóa bảng `properties` rồi restart để seed lại 29 phòng test.
- **Session:** Mỗi tab trình duyệt = 1 session riêng. Nếu muốn demo lại từ đầu: mở tab ẩn danh mới, hoặc kết thúc session bằng `exit`.
- **Rate limit:** Mặc định 20 requests / 10 phút trên mỗi session. Kịch bản 1 dùng ~14 turns, kịch bản 2 ~5 turns — an toàn trong giới hạn.
- **Gemini model:** `gemini-2.0-flash` (v1beta). Nếu API trả về lỗi, thử lại sau vài giây hoặc kiểm tra quota trong Google AI Studio.
