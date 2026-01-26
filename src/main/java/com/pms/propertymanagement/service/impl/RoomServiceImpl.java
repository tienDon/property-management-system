package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.RoomSearchRequest;
import com.pms.propertymanagement.dto.response.RoomSearchResponse;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.mapper.RoomMapper;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id = " + id));
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findByIsDeletedFalseAndStatus("AVAILABLE");
    }

    @Override
    public List<Room> searchRooms(
            String provinceCode,
            String districtCode,
            String wardCode,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            Double minArea,
            Double maxArea
    ) {
        return roomRepository.searchRooms(
                provinceCode,
                districtCode,
                wardCode,
                categoryId,
                minPrice,
                maxPrice,
                minArea,
                maxArea
        );
    }

}
