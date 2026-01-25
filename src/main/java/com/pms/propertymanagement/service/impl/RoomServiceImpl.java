package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    public Room getById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
    }
}
