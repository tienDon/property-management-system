package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.response.DistrictResponse;
import com.pms.propertymanagement.repository.DistrictRepository;
import com.pms.propertymanagement.service.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistrictServiceImpl implements DistrictService {

    @Autowired
    private DistrictRepository districtRepository;

    @Override
    public List<DistrictResponse> findByProvince_Code(String provinceCode) {
        return districtRepository.findByProvince_Code(provinceCode)
                .stream()
                .map(d -> new DistrictResponse(d.getCode(), d.getName()))
                .toList();
    }
}
