package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.ContractRequest;
import com.pms.propertymanagement.entity.Contract;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.Tenant;
import com.pms.propertymanagement.enums.ContractStatus;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.ContractRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.repository.TenantRepository;
import com.pms.propertymanagement.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public Contract createContract(ContractRequest dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Contract contract = new Contract();
        contract.setRoom(room);
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setRentPrice(dto.getRentPrice());
        contract.setDeposit(dto.getDeposit());
        contract.setPaymentCycle(dto.getPaymentCycle());
        contract.setIsElectricityWaterIncluded(dto.getIsElectricityWaterIncluded());
        contract.setBedCount(dto.getBedCount());
        contract.setNote(dto.getNote());
        contract.setStatus(ContractStatus.ACTIVE);

        if (dto.getTenantIds() != null && !dto.getTenantIds().isEmpty()) {
            List<Tenant> tenants = tenantRepository.findAllById(dto.getTenantIds());
            if (!tenants.isEmpty()) {
                // Ensure order of tenants matches the selection order (first one is representative)
                Map<Long, Tenant> tenantMap = tenants.stream()
                        .collect(Collectors.toMap(Tenant::getId, Function.identity()));
                
                List<Tenant> orderedTenants = dto.getTenantIds().stream()
                        .filter(tenantMap::containsKey) // Filter out any invalid IDs
                        .map(tenantMap::get)
                        .collect(Collectors.toList());

                if (!orderedTenants.isEmpty()) {
                    contract.setRepresentative(orderedTenants.get(0)); // First selected is representative
                    contract.setTenants(new HashSet<>(orderedTenants));
                }
            }
        }
        
        // Copy services from room to contract
        if(room.getServices() != null){
             contract.setServices(new HashSet<>(room.getServices()));
        }

        // Update Room Status
        room.setStatus(RoomStatus.RENTED);
        roomRepository.save(room);

        return contractRepository.save(contract);
    }

    @Override
    public Page<Contract> getContractsByOwner(Long ownerId, ContractStatus status, String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return contractRepository.searchByOwnerIdAndStatusAndKeyword(ownerId, status, keyword, pageable);
        }
        return contractRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
    }

    @Override
    public Contract getContractById(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    @Override
    @Transactional
    public void updateContractStatus(Long id, ContractStatus status) {
        Contract contract = getContractById(id);
        contract.setStatus(status);
        contract.setUpdatedAt(LocalDateTime.now());
        
        // If contract is terminated/expired, maybe room becomes AVAILABLE?
        if(status == ContractStatus.TERMINATED || status == ContractStatus.EXPIRED){
            Room room = contract.getRoom();
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
        }
        
        contractRepository.save(contract);
    }
}
