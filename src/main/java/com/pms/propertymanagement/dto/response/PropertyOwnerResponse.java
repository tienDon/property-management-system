package com.pms.propertymanagement.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyOwnerResponse {
    private Long id;
    private String name; // Thêm tên nhà trọ
    private String title;
    private String addressNumber; // Chủ nhà cần thấy địa chỉ cụ thể
    private String categoryName;
    private String formattedCreatedAt; // Ngày đăng bài
    private String img_url; // Thumbnail nhỏ để chủ nhà nhận diện phòng
    // Có thể thêm: private String status; (Đang hiển thị/Đã ẩn)
}