package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.request.RoomRequest;
import com.pms.propertymanagement.dto.response.RoomResponse;
import com.pms.propertymanagement.entity.User;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getAllRoomsByOwner(User owner);
    void createRoom(RoomRequest request, User owner);
    void deleteRoom(Long id);
    List<RoomResponse> getAvailableRoomsByProperty(Long propertyId);
}
