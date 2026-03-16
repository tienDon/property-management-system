package com.pms.propertymanagement.dto.response;

import com.pms.propertymanagement.dto.request.ServiceItemDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailResponse {

    // Thông tin cơ bản (Property table)
    private Long id;
    private String title;
    private String name;
    private double price;
    private double acreage;
    private String description; // Chính là phần "Giới thiệu" trong hình
    private String addressNumber;
    private String wardName;
    private String provinceName; // Tỉnh/Thành phố
    private String formattedCreatedAt; // "Ngày đăng: 27-01-2026"
    private String slug; 

    private int numberOfRooms;

    // Map Coordinates
    private Double latitude;
    private Double longitude;

    // Rules & Costs
    private List<String> rules;
    private List<ServiceItemDTO> serviceItems;

    // Thông tin danh mục
    private String categoryName;

    // Danh sách hình ảnh & video (PropertyImage table)
    private List<String> imageUrls;

    // Tiện ích & Môi trường (Amenities table)
    // Tách ra để dễ hiển thị icon theo từng nhóm như trong hình 2
    private List<IconResponse> targetTenants;
    private List<IconResponse> amenities; // Wifi, Máy giặt, Điều hòa...
    private List<IconResponse> surroundings; // Chợ, Siêu thị, Trường học...


    // Thông tin chủ nhà (User table)
    private String ownerName;
    private String ownerPhone;

}
