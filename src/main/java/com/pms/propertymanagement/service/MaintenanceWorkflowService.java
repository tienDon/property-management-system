package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.User;

import java.util.List;

public interface MaintenanceWorkflowService {
    List<MaintenanceRequest> getRequestsForOwner(User owner);
    MaintenanceRequest getRequestDetailForOwner(Long id, User owner);
    MaintenanceRequest assignRequest(Long id, User owner, Long staffId);
    MaintenanceRequest rejectRequest(Long id, User owner, String reason);

    List<MaintenanceRequest> getRequestsForStaff(User staff);
    MaintenanceRequest getRequestDetailForStaff(Long id, User staff);
    MaintenanceRequest startRequest(Long id, User staff);
    MaintenanceRequest completeRequest(Long id, User staff, String note);
}
