package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TenantMaintenanceService {
    List<Room> getRoomsForTenant(User tenant);

    List<Room> getAvailableRooms();

    Room getRoomDetail(Long roomId);

    boolean isRoomRentedByTenant(Long roomId, User tenant);

    List<MaintenanceRequest> getRequestsForTenant(User tenant);

    MaintenanceRequest createRequest(Long roomId, User tenant, MaintenanceCategory category, String description,
            MultipartFile image);

    MaintenanceRequest getTenantRequestDetail(Long id, User tenant);

    MaintenanceRequest confirmCompletion(Long id, User tenant);

    MaintenanceRequest reopenRequest(Long id, String reason, User tenant);
}
