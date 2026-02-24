package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    @EntityGraph(attributePaths = {"room"})
    Optional<MaintenanceRequest> findByIdAndTenant_Id(Long id, Long tenantId);

    @EntityGraph(attributePaths = {"room"})
    List<MaintenanceRequest> findByTenant_IdOrderByCreatedAtDesc(Long tenantId);
    List<MaintenanceRequest> findByRoom_IdOrderByCreatedAtDesc(Long roomId);
}

