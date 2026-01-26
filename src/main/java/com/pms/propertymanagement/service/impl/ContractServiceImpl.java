package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Contract;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.repository.ContractRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.service.ContractService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;

    @Override
    public Contract createContract(Contract contract) {
        Objects.requireNonNull(contract.getRoom(), "Room is required");
        Objects.requireNonNull(contract.getTenant(), "Tenant is required");
        validateDates(contract);

        contract.setStatus("PENDING");
        appendHistory(contract, "CREATED", "Khởi tạo hợp đồng");
        if (contract.getMonthlyRent() == null && contract.getRoom() != null) {
            contract.setMonthlyRent(contract.getRoom().getPrice());
        }

        return contractRepository.save(contract);
    }

    @Override
    public Contract approveContract(Long id, Long userId) {
        Contract contract = getById(id);
        contract.setStatus("APPROVED");
        appendHistory(contract, "APPROVED", "Phê duyệt hợp đồng");
        return contractRepository.save(contract);
    }

    @Override
    public Contract activateContract(Long id, Long userId) {
        Contract contract = getById(id);
        contract.setStatus("ACTIVE");
        appendHistory(contract, "ACTIVE", "Kích hoạt hợp đồng");
        Contract saved = contractRepository.save(contract);

        Room room = contract.getRoom();
        if (room != null) {
            room.setStatus("RENTED");
            roomRepository.save(room);
        }
        return saved;
    }

    @Override
    public Contract getById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    private void validateDates(Contract contract) {
        LocalDate start = contract.getStartDate();
        LocalDate end = contract.getEndDate();
        if (start == null || end == null) {
            throw new RuntimeException("Ngày bắt đầu/kết thúc không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private void appendHistory(Contract contract, String action, String note) {
        String entry = action + " - " + note;
        String current = contract.getHistoryNote();
        if (current == null || current.isBlank()) {
            contract.setHistoryNote(entry);
            return;
        }
        contract.setHistoryNote(current + "\n" + entry);
    }
}
