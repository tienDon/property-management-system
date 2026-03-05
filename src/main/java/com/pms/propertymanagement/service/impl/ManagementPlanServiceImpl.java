package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.ManagementPlanDTO;
import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.mapper.ManagementPlanMapper;
import com.pms.propertymanagement.repository.ManagementPlanRepository;
import com.pms.propertymanagement.service.ManagementPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementPlanServiceImpl implements ManagementPlanService {

    private final ManagementPlanRepository managementPlanRepository;
    private final ManagementPlanMapper managementPlanMapper;

    @Override
    public ManagementPlan getDefaultPlan() {
        return managementPlanRepository.findByCode("FREE")
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói FREE. Hãy kiểm tra initializer."));
    }

    @Override
    public ManagementPlan getByCode(String code) {
        return managementPlanRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói: " + code));
    }

    @Override
    public ManagementPlan getById(Long id) {
        return managementPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy plan id=" + id));
    }

    @Override
    public List<ManagementPlan> findAllActive() {
        return managementPlanRepository.findAllActiveOrderBySortOrder();
    }

    @Override
    public List<ManagementPlan> findAll() {
        return managementPlanRepository.findAllByOrderBySortOrderAsc();
    }
    
    @Override
    public List<ManagementPlanDTO> getAllPlans() {
        return findAllActive().stream()
                .map(managementPlanMapper::toDTO)
                .toList();
    }

    @Override
    public boolean canUpgradeTo(String fromPlanCode, String toPlanCode) {
        ManagementPlan fromPlan = getByCode(fromPlanCode);
        ManagementPlan toPlan = getByCode(toPlanCode);
        
        // Can only upgrade to higher tier or same tier
        return toPlan.getSortOrder() >= fromPlan.getSortOrder();
    }
}