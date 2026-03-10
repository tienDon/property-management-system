package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.MaintenanceRequestLog;
import com.pms.propertymanagement.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRequestLogRepository extends JpaRepository<MaintenanceRequestLog, Long> {
    long countByRequest_IdAndStatus(Long requestId, MaintenanceStatus status);
}

