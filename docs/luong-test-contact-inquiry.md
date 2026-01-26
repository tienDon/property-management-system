# Luồng test Contact Inquiry (liên hệ chủ phòng)

## Mục tiêu

- Test đầy đủ luồng gửi yêu cầu liên hệ theo phòng.
- Xác minh nội dung tiếng Việt không bị lỗi kiểu `Kh?i t?o...` ở phần message.

## Điều kiện

- App chạy được và kết nối SQL Server OK.
- Nếu DB có schema cũ từng dùng `VARCHAR` cho các trường text, chạy script đổi sang Unicode:
  - [unicode-columns.sql](file:///d:/Documents/GIT_HUB/property-management-system/scripts/sqlserver/unicode-columns.sql)
  - Nếu không chạy script, trang `/contact/received` có thể hiện lỗi do DB chưa đúng kiểu cột.

## Chạy ứng dụng (khi 8080 đang bận)

```bash
.\mvnw.cmd -DskipTests package
java -jar .\target\property-management-0.0.1-SNAPSHOT.jar --server.port=8081
```

## Luồng test (UI)

### 1) Mở chi tiết phòng

Mở (ví dụ phòng id=1):

- http://localhost:8081/rooms/1

Kỳ vọng:

- Trang hiển thị thông tin phòng và có nút `Contact Owner`.

### 2) Mở form liên hệ

Bấm `Contact Owner` hoặc mở trực tiếp:

- http://localhost:8081/contact/room/1

Kỳ vọng:

- Form hiển thị thông tin phòng (room number, giá, địa chỉ) và các trường:
  - Phone Number (bắt buộc)
  - Email (tùy chọn)
  - Message (bắt buộc, tối thiểu 10 ký tự)

### 3) Gửi inquiry (có tiếng Việt)

Nhập ví dụ:

- Phone Number: `0909123456`
- Email: `a@b.com` (tuỳ chọn)
- Message: `Mình muốn xem phòng vào chiều thứ 7. Có thể hẹn lúc 14:00 không ạ?`

Bấm `Send Inquiry`.

Kỳ vọng:

- Redirect về `Room Detail` và có flash message: `Đã gửi yêu cầu liên hệ thành công!`

### 4) Host xem danh sách contact nhận

Mở danh sách:

- http://localhost:8081/contact/received

Hoặc lọc theo host:

- http://localhost:8081/contact/received?hostId={hostId}

Kỳ vọng:

- Bảng có dòng inquiry vừa gửi.
- Cột `Message Preview` hiển thị tiếng Việt đúng dấu.

### 5) Xem chi tiết inquiry

Từ danh sách, bấm `Xem`.

Kỳ vọng:

- Trang chi tiết hiển thị `Message Content` đúng tiếng Việt.
- Không có chuỗi bị biến dạng kiểu `Kh?i`.

### 6) Cập nhật trạng thái

Từ danh sách nhận, dùng dropdown đổi `PENDING → CONTACTED → CLOSED`, bấm `Cập nhật`.

Kỳ vọng:

- Flash message: `Đã cập nhật trạng thái!`
- Badge trạng thái đổi đúng theo giá trị chọn.

### 7) Tạo hợp đồng từ inquiry (nhánh phụ)

Trong danh sách nhận, bấm `Tạo hợp đồng`.

Kỳ vọng:

- Điều hướng tới form tạo hợp đồng theo `roomId`.

## Luồng test (HTTP nhanh)

### Gửi inquiry

```powershell
$roomId = 1
$body = "contactPhone=0909123456&contactEmail=a%40b.com&message=$([uri]::EscapeDataString('Mình muốn xem phòng vào chiều thứ 7. Có thể hẹn lúc 14:00 không ạ?'))"
Invoke-WebRequest -UseBasicParsing -Method Post -Uri "http://localhost:8081/contact/room/$roomId" -ContentType "application/x-www-form-urlencoded" -Body $body
```

### Lấy inquiryId từ trang danh sách và kiểm tra tiếng Việt

```powershell
$html = (Invoke-WebRequest -UseBasicParsing http://localhost:8081/contact/received).Content
if ($html -match "Kh\?i") { "MOJIBAKE_FOUND" } else { "NO_MOJIBAKE_MATCH" }
```

## Troubleshooting

### `/contact/received` trả 500 hoặc không load được danh sách

- Nguyên nhân thường gặp: các cột text của `contact_inquiries` vẫn là `VARCHAR` nhưng entity đang đọc theo Unicode.
- Cách xử lý:
  - Chạy [unicode-columns.sql](file:///d:/Documents/GIT_HUB/property-management-system/scripts/sqlserver/unicode-columns.sql)
  - Sau đó reload lại `/contact/received`.
