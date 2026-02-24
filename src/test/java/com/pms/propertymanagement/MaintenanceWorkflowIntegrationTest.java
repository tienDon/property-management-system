package com.pms.propertymanagement;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.enums.MaintenanceStatus;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.MaintenanceWorkflowService;
import com.pms.propertymanagement.service.TenantMaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MaintenanceWorkflowIntegrationTest {

    @Autowired
    private TenantMaintenanceService tenantMaintenanceService;

    @Autowired
    private MaintenanceWorkflowService maintenanceWorkflowService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void ownerAssignsAndStaffCompletesRequest() {
        User tenant = userRepository.findByUsername("tenant1").orElseThrow();
        Room room = tenantMaintenanceService.getRoomsForTenant(tenant).stream().findFirst().orElseThrow();

        MaintenanceRequest created = tenantMaintenanceService.createRequest(
                room.getId(),
                tenant,
                MaintenanceCategory.ELECTRICAL,
                "Test workflow"
        );
        assertNotNull(created.getId());
        assertEquals(MaintenanceStatus.PENDING, created.getStatus());

        User owner = userRepository.findByUsername("owner1").orElseThrow();
        User staff = userRepository.findByUsername("staff1").orElseThrow();

        MaintenanceRequest assigned = maintenanceWorkflowService.assignRequest(created.getId(), owner, staff.getId());
        assertEquals(MaintenanceStatus.ASSIGNED, assigned.getStatus());
        assertNotNull(assigned.getStaff());
        assertNotNull(assigned.getAssignedAt());

        MaintenanceRequest inProgress = maintenanceWorkflowService.startRequest(created.getId(), staff);
        assertEquals(MaintenanceStatus.IN_PROGRESS, inProgress.getStatus());
        assertNotNull(inProgress.getStartedAt());

        MaintenanceRequest completed = maintenanceWorkflowService.completeRequest(created.getId(), staff, "Done");
        assertEquals(MaintenanceStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
        assertEquals("Done", completed.getCompletionNote());
    }
}
