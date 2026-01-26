package com.pms.propertymanagement.service;


import com.pms.propertymanagement.dto.request.RoomSearchRequest;
import com.pms.propertymanagement.dto.response.RoomSearchResponse;
import com.pms.propertymanagement.entity.Room;

import java.util.List;

public interface RoomService {
    Room getById(Long id);

    List<Room> getAllRooms();

    List<Room> searchRooms(
            String provinceCode,
            String districtCode,
            String wardCode,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            Double minArea,
            Double maxArea
    );
}
