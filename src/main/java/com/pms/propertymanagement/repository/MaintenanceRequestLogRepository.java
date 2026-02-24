package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.MaintenanceRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRequestLogRepository extends JpaRepository<MaintenanceRequestLog, Long> {
}

