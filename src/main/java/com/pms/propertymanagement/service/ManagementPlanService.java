package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.ManagementPlanDTO;
import com.pms.propertymanagement.entity.ManagementPlan;

import java.util.List;

public interface ManagementPlanService {

    ManagementPlan getDefaultPlan(); // FREE
    
    ManagementPlan getByCode(String code);
    
    ManagementPlan getById(Long id);
    
    List<ManagementPlan> findAllActive();
    
    List<ManagementPlan> findAll();
    
    List<ManagementPlanDTO> getAllPlans();
    
    boolean canUpgradeTo(String fromPlanCode, String toPlanCode);
}