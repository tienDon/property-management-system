package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByOwner_Username(String username);
    List<Tenant> findByOwner_Id(Long ownerId); // Changed to underscore for JPA relationship safety
    boolean existsByCitizenId(String citizenId);
}
