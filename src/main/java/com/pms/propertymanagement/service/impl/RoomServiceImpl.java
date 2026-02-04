package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.RoomRequest;
import com.pms.propertymanagement.dto.response.RoomResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.ServiceItem;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.repository.ServiceItemRepository;
import com.pms.propertymanagement.service.RoomService;
import com.pms.propertymanagement.utils.CurrencyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Override
    public List<RoomResponse> getAllRoomsByOwner(User owner) {
        List<Room> rooms = roomRepository.findAllByOwnerUsername(owner.getUsername());
        return rooms.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createRoom(RoomRequest request, User owner) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Validate owner owns the property
        if (!property.getOwner().getUsername().equals(owner.getUsername())) {
             throw new RuntimeException("Unauthorized");
        }

        Room room = new Room();
        room.setName(request.getName());
        room.setPrice(request.getPrice());
        room.setDeposit(request.getDeposit());
        room.setArea(request.getArea());
        room.setMaxOccupancy(request.getMaxOccupancy());
        room.setBedCount(request.getBedCount());
        room.setPaymentCycle(request.getPaymentCycle());
        room.setIsElectricityWaterIncluded(request.getIsElectricityWaterIncluded());
        room.setDescription(request.getDescription());
        room.setProperty(property);
        room.setStatus(RoomStatus.AVAILABLE);

        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<ServiceItem> services = serviceItemRepository.findAllById(request.getServiceIds());
            room.setServices(new HashSet<>(services));
        }

        roomRepository.save(room);
        
        // Update property room count if needed? No, database might trigger or just count query.
        // But the Property entity has numberOfRooms field.
        property.setNumberOfRooms(property.getNumberOfRooms() + 1);
        propertyRepository.save(property);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        Property property = room.getProperty();
        roomRepository.deleteById(id);
        
        // Update count
        if (property.getNumberOfRooms() > 0) {
            property.setNumberOfRooms(property.getNumberOfRooms() - 1);
            propertyRepository.save(property);
        }
    }

    @Override
    public List<RoomResponse> getAvailableRoomsByProperty(Long propertyId) {
        List<Room> rooms = roomRepository.findByPropertyIdAndStatus(propertyId, RoomStatus.AVAILABLE);
        return rooms.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private RoomResponse mapToResponse(Room room) {
        String statusStyle = switch (room.getStatus()) {
            case AVAILABLE -> "bg-green-100 text-green-700";
            case RENTED -> "bg-red-100 text-red-700";
            case MAINTENANCE -> "bg-yellow-100 text-yellow-700";
        };

        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .price(room.getPrice())
                .formattedPrice(CurrencyUtil.formatVND(room.getPrice()))
                .deposit(room.getDeposit())
                .area(room.getArea())
                .maxOccupancy(room.getMaxOccupancy())
                .propertyName(room.getProperty().getName())
                .status(room.getStatus().getDisplayName())
                .statusStyle(statusStyle)
                .build();
    }
}
