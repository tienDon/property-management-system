package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.TargetTenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetTenantsRepository extends JpaRepository<TargetTenant,Long> {
}
