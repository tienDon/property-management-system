package com.pms.propertymanagement;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.repository.RoomRepository;
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

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void createRequest_generatesCodeAndPersists() {
        User tenant = userRepository.findAll().stream().findFirst().orElseThrow();
        Room room = roomRepository.findAll().stream().findFirst().orElseThrow();

        MaintenanceRequest saved = tenantMaintenanceService.createRequest(
                room.getId(),
                tenant,
                MaintenanceCategory.ELECTRICAL,
                "Test yêu cầu bảo trì"
        );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCode());
    }
}

