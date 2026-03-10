package com.pms.propertymanagement.enums;

public enum GeocodeStatus {
    PENDING,          // mới tạo, chưa gọi API
    SUCCESS,          // API trả về thành công, lat/lng đã có
    FAIL,             // API fail hoặc confidence < 5, lat/lng = null
    MANUAL_OVERRIDE   // admin đã sửa tay → không overwrite bằng API nữa
}
