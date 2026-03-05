package com.pms.propertymanagement;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.TenantMaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MaintenanceRequestCreateIntegrationTest {

    @Autowired
    private TenantMaintenanceService tenantMaintenanceService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createRequest_generatesCodeAndPersists() {
        User tenant = userRepository.findByUsername("tenant1").orElseThrow();
        Room room = tenantMaintenanceService.getRoomsForTenant(tenant).stream().findFirst().orElseThrow();

        MaintenanceRequest saved = tenantMaintenanceService.createRequest(
                room.getId(),
                tenant,
                MaintenanceCategory.ELECTRICAL,
                "Test yêu cầu bảo trì",
                null);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCode());
    }
}
