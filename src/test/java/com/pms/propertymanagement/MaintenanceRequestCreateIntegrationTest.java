package com.pms.propertymanagement;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.enums.MaintenanceStatus;
import com.pms.propertymanagement.repository.MaintenanceRequestRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.service.TenantMaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MaintenanceRequestCreateIntegrationTest {

    @Autowired
    private TenantMaintenanceService tenantMaintenanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceRequestRepository maintenanceRequestRepository;

    @Test
    void createRequest_generatesCodeAndPersists() {
        User tenant = userRepository.findByUsername("tenant1").orElseThrow();
        Room room = tenantMaintenanceService.getRoomsForTenant(tenant).stream().findFirst().orElseThrow();

        List<MaintenanceStatus> unresolved = List.of(
                MaintenanceStatus.PENDING,
                MaintenanceStatus.ASSIGNED,
                MaintenanceStatus.IN_PROGRESS
        );
        List<MaintenanceRequest> existing = maintenanceRequestRepository.findByRoom_IdOrderByCreatedAtDesc(room.getId())
                .stream()
                .filter(r -> unresolved.contains(r.getStatus()))
                .toList();
        if (!existing.isEmpty()) {
            existing.forEach(r -> {
                r.setStatus(MaintenanceStatus.COMPLETED);
                r.setUpdatedAt(LocalDateTime.now());
            });
            maintenanceRequestRepository.saveAll(existing);
        }

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
