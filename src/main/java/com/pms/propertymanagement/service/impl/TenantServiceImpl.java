package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.request.TenantRequest;
import com.pms.propertymanagement.dto.response.TenantResponse;
import com.pms.propertymanagement.entity.Tenant;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.TenantRepository;
import com.pms.propertymanagement.service.TenantService;
import com.pms.propertymanagement.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public List<TenantResponse> getAllTenantsByOwner(User owner) {
        List<Tenant> tenants = tenantRepository.findByOwner_Id(owner.getId());

        return tenants.stream().map(t -> TenantResponse.builder()
                .id(t.getId())
                .fullName(t.getFullName())
                .phone(t.getPhone())
                .citizenId(t.getCitizenId())
                .gender(t.getGender())
                .career(t.getCareer())
                .permanentAddress(t.getPermanentAddress())
                .formattedBirthday(t.getBirthday() != null ? DateUtil.formatDate(t.getBirthday()) : "")
                .formattedCreatedAt(DateUtil.formatDateTime(t.getCreatedAt()))
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void createTenant(TenantRequest request, User owner) {
        // Kiểm tra CCCD trùng (nếu cần thiết, hoặc handle exception DB)
        if (tenantRepository.existsByCitizenId(request.getCitizenId())) {
           // Có thể throw exception hoặc xử lý logic riêng
           // throw new RuntimeException("CCCD này đã tồn tại trong hệ thống");
        }

        Tenant tenant = new Tenant();
        tenant.setFullName(request.getFullName());
        tenant.setPhone(request.getPhone());
        tenant.setEmail(request.getEmail());
        tenant.setCitizenId(request.getCitizenId());
        tenant.setGender(request.getGender());
        tenant.setCareer(request.getCareer());
        tenant.setPermanentAddress(request.getPermanentAddress());
        tenant.setPlaceOfIssue(request.getPlaceOfIssue());
        tenant.setBirthday(request.getBirthday());
        tenant.setIssueDate(request.getIssueDate());
        
        tenant.setOwner(owner);

        tenantRepository.save(tenant);
    }

    @Override
    public void deleteTenant(Long id) {
        tenantRepository.deleteById(id);
    }
}
