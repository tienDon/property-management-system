package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.enums.ContractStatus;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContractInitializer {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Transactional
    public void init() {
        User owner = userRepository.findByUsername("owner1").orElse(null);
        if (owner == null) return;

        // Skip if contracts exist
        if (contractRepository.count() > 0) return;

        // Get all tenants of owner1
        List<Tenant> tenants = tenantRepository.findByOwner_Id(owner.getId());
        if (tenants.isEmpty()) return;

        // Get all rooms of owner1, prefer RENTED ones
        List<Room> allRooms = roomRepository.findByProperty_Owner_Id(owner.getId());
        List<Room> rentedRooms = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.RENTED)
                .collect(Collectors.toList());

        if (rentedRooms.isEmpty()) {
            // If no rented rooms (maybe random generator didn't pick any), force picking some
            for (int i = 0; i < Math.min(5, allRooms.size()); i++) {
                Room r = allRooms.get(i);
                r.setStatus(RoomStatus.RENTED);
                rentedRooms.add(r);
            }
            roomRepository.saveAll(rentedRooms);
        }

        Random random = new Random();
        
        for (Room room : rentedRooms) {
            Contract contract = new Contract();
            
            // Randomly assign a representative from existing tenants
            Tenant representative = tenants.get(random.nextInt(tenants.size()));
            contract.setRepresentative(representative);
            
            // Tenants in the room (including representative + maybe others)
            Set<Tenant> contractTenants = new HashSet<>();
            contractTenants.add(representative);
            
            // Maybe add another tenant if max occupancy allows
            if (room.getMaxOccupancy() > 1 && tenants.size() > 1 && random.nextBoolean()) {
                Tenant another = tenants.get(random.nextInt(tenants.size()));
                if (!another.getId().equals(representative.getId())) {
                    contractTenants.add(another);
                }
            }
            contract.setTenants(contractTenants);
            
            contract.setRoom(room);
            
            // Set random dates (e.g., started 1-6 months ago, duration 6-12 months)
            LocalDate startDate = LocalDate.now().minusMonths(random.nextInt(6) + 1);
            LocalDate endDate = startDate.plusMonths(6 + random.nextInt(7)); // 6 to 12 months contract
            
            contract.setStartDate(startDate);
            contract.setEndDate(endDate);
            
            contract.setRentPrice(room.getPrice());
            contract.setDeposit(room.getDeposit() != null ? room.getDeposit() : room.getPrice());
            contract.setPaymentCycle(room.getPaymentCycle() != null ? room.getPaymentCycle() : 1);
            contract.setIsElectricityWaterIncluded(room.getIsElectricityWaterIncluded());
            contract.setBedCount(room.getBedCount());
            contract.setCode("HD-" + room.getId() + "-" + System.currentTimeMillis() % 100000);
            
            // Inherit services from Room
            if (room.getServices() != null) {
                contract.setServices(new HashSet<>(room.getServices()));
            }
            
            // Determine status based on dates
            if (endDate.isBefore(LocalDate.now())) {
                contract.setStatus(ContractStatus.EXPIRED);
            } else if (endDate.isBefore(LocalDate.now().plusDays(30))) {
                contract.setStatus(ContractStatus.EXPIRING_SOON);
            } else {
                contract.setStatus(ContractStatus.ACTIVE);
            }
            
            contract.setNote("Hợp đồng tạo mẫu tự động.");
            
            contractRepository.save(contract);
        }
        
        System.out.println("Generated " + rentedRooms.size() + " contracts for owner1.");
    }
}
