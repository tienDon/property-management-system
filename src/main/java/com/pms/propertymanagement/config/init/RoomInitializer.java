package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.ServiceItem;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class RoomInitializer {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Transactional
    public void init() {
        if (roomRepository.count() > 0) return;

        List<Property> properties = propertyRepository.findAll();
        if (properties.isEmpty()) return;

        Random random = new Random();
        List<Room> rooms = new ArrayList<>();

        for (Property p : properties) {
            int roomCount = p.getNumberOfRooms();
            if (roomCount <= 0) roomCount = 1;

            // Fetch services for this property to assign to rooms
            List<ServiceItem> propertyServices = serviceItemRepository.findByProperty_Id(p.getId());

            for (int i = 1; i <= roomCount; i++) {
                Room room = new Room();
                
                String name;
                if (roomCount >= 10) {
                    // Logic tạo số phòng kiểu lầu (ví dụ 10 phòng -> 2 lầu, mỗi lầu 5 phòng)
                    int floor = (i - 1) / 5 + 1;
                    int roomNum = (i - 1) % 5 + 1;
                    name = String.format("P.%d0%d", floor, roomNum);
                } else {
                    name = "Phòng " + i;
                }
                
                room.setName(name);
                room.setProperty(p);
                room.setPrice((double) p.getPrice()); 
                room.setArea(p.getAcreage());
                
                boolean isDorm = p.getCategory() != null && p.getCategory().getName().equalsIgnoreCase("Ký túc xá");
                
                room.setMaxOccupancy(isDorm ? 4 : 2); 
                room.setBedCount(isDorm ? 4 : 1);
                
                room.setDeposit(p.getPrice() * 1.0); 
                room.setPaymentCycle(1); 
                room.setIsElectricityWaterIncluded(false);
                
                // Random Status
                int statusRoll = random.nextInt(10);
                if (statusRoll < 3) {
                    room.setStatus(RoomStatus.AVAILABLE);
                } else if (statusRoll < 9) {
                    room.setStatus(RoomStatus.RENTED);
                } else {
                    room.setStatus(RoomStatus.MAINTENANCE);
                }
                
                room.setDescription("Phòng đầy đủ ánh sáng, thoáng mát.");

                if (!propertyServices.isEmpty()) {
                    room.setServices(new HashSet<>(propertyServices));
                }
                
                room.setCreatedAt(LocalDateTime.now());
                rooms.add(room);
            }
        }

        roomRepository.saveAll(rooms);
        System.out.println("Generated " + rooms.size() + " sample rooms.");
    }
}
