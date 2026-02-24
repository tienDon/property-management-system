package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.MaintenanceRequestLog;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.Tenant;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.enums.MaintenanceStatus;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.repository.ContractRepository;
import com.pms.propertymanagement.repository.MaintenanceRequestLogRepository;
import com.pms.propertymanagement.repository.MaintenanceRequestRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.repository.TenantRepository;
import com.pms.propertymanagement.service.TenantMaintenanceService;
import lombok.RequiredArgsConstructor;
import com.pms.propertymanagement.exception.ForbiddenException;
import com.pms.propertymanagement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantMaintenanceServiceImpl implements TenantMaintenanceService {

    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final TenantRepository tenantRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceRequestLogRepository logRepository;

    @Override
    public List<Room> getRoomsForTenant(User tenant) {
        Optional<Tenant> tenantProfile = findTenantProfile(tenant);
        if (tenantProfile.isEmpty()) {
            return List.of();
        }
        return contractRepository.findActiveRoomsByTenantId(tenantProfile.get().getId());
    }

    @Override
    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus(RoomStatus.AVAILABLE);
    }

    @Override
    public Room getRoomDetail(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    @Override
    public boolean isRoomRentedByTenant(Long roomId, User tenant) {
        if (tenant == null || roomId == null) {
            return false;
        }
        Optional<Tenant> tenantProfile = findTenantProfile(tenant);
        if (tenantProfile.isEmpty()) {
            return false;
        }
        return contractRepository.countActiveContractsByTenantIdAndRoomId(tenantProfile.get().getId(), roomId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getRequestsForTenant(User tenant) {
        return maintenanceRequestRepository.findByTenant_IdOrderByCreatedAtDesc(tenant.getId());
    }

    @Override
    @Transactional
    public MaintenanceRequest createRequest(Long roomId, User tenant, MaintenanceCategory category, String description) {
        if (tenant == null) {
            throw new IllegalArgumentException("Bạn chưa đăng nhập");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("Phòng không hợp lệ");
        }
        if (category == null || !StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Danh mục và mô tả không được trống");
        }
        if (!isRoomRentedByTenant(roomId, tenant)) {
            throw new ForbiddenException("Bạn chỉ có thể tạo yêu cầu cho phòng đang thuê");
        }
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        MaintenanceRequest req = new MaintenanceRequest();
        req.setCode(generateRequestCode());
        req.setRoom(room);
        req.setTenant(tenant);
        req.setCategory(category);
        req.setDescription(description);
        req.setStatus(MaintenanceStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        MaintenanceRequest saved = maintenanceRequestRepository.save(req);

        MaintenanceRequestLog log = new MaintenanceRequestLog();
        log.setRequest(saved);
        log.setActor(tenant);
        log.setStatus(MaintenanceStatus.PENDING);
        log.setNote("Tenant tạo yêu cầu");
        logRepository.save(log);

        return saved;
    }

    private String generateRequestCode() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "MR-" + ts + "-" + rand;
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRequest getTenantRequestDetail(Long id, User tenant) {
        return maintenanceRequestRepository.findByIdAndTenant_Id(id, tenant.getId())
                .orElseThrow(() -> new ForbiddenException("Bạn không có quyền truy cập yêu cầu này"));
    }

    @Override
    @Transactional
    public MaintenanceRequest confirmCompletion(Long id, User tenant) {
        MaintenanceRequest req = getTenantRequestDetail(id, tenant);
        if (req.getStatus() != MaintenanceStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ xác nhận khi trạng thái là COMPLETED");
        }
        req.setStatus(MaintenanceStatus.CONFIRMED);
        req.setUpdatedAt(LocalDateTime.now());
        MaintenanceRequest saved = maintenanceRequestRepository.save(req);

        MaintenanceRequestLog log = new MaintenanceRequestLog();
        log.setRequest(saved);
        log.setActor(tenant);
        log.setStatus(MaintenanceStatus.CONFIRMED);
        log.setNote("Tenant xác nhận hoàn thành");
        logRepository.save(log);

        return saved;
    }

    @Override
    @Transactional
    public MaintenanceRequest reopenRequest(Long id, String reason, User tenant) {
        if (!StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("Lý do không được trống");
        }
        MaintenanceRequest req = getTenantRequestDetail(id, tenant);
        if (req.getStatus() != MaintenanceStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ mở lại khi trạng thái là COMPLETED");
        }
        req.setStatus(MaintenanceStatus.REOPENED);
        req.setReopenedReason(reason);
        req.setUpdatedAt(LocalDateTime.now());
        MaintenanceRequest saved = maintenanceRequestRepository.save(req);

        MaintenanceRequestLog log = new MaintenanceRequestLog();
        log.setRequest(saved);
        log.setActor(tenant);
        log.setStatus(MaintenanceStatus.REOPENED);
        log.setNote(reason);
        logRepository.save(log);

        return saved;
    }

    private Optional<Tenant> findTenantProfile(User tenantUser) {
        if (tenantUser == null) {
            return Optional.empty();
        }
        if (StringUtils.hasText(tenantUser.getPhone())) {
            Optional<Tenant> byPhone = tenantRepository.findFirstByPhone(tenantUser.getPhone().trim());
            if (byPhone.isPresent()) {
                return byPhone;
            }
        }
        if (StringUtils.hasText(tenantUser.getEmail())) {
            return tenantRepository.findFirstByEmail(tenantUser.getEmail().trim());
        }
        return Optional.empty();
    }
}

