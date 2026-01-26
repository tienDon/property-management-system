package com.pms.propertymanagement.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RoomSearchRequest {

    @NotBlank(message = "Bạn phải chọn Tỉnh/TP")
    private String provinceId;

    @NotBlank(message = "Bạn phải chọn Quận/Huyện")
    private String districtId;

    @NotBlank(message = "Bạn phải chọn Phường/Xã")
    private String wardId;

    @NotNull(message = "Bạn phải chọn loại trọ")
    private Long categoryId;

    @NotBlank(message = "Bạn phải chọn mức giá")
    private String priceRange;   // VD: "2000000-4000000"

    @NotBlank(message = "Bạn phải chọn diện tích")
    private String areaRange;    // VD: "20-35"
}
