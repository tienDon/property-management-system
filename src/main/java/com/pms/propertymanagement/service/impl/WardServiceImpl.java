package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.response.WardResponse;
import com.pms.propertymanagement.repository.WardRepository;
import com.pms.propertymanagement.service.WardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WardServiceImpl implements WardService {

    @Autowired
    private WardRepository wardRepository;

    @Override
    public List<WardResponse> findByDistrict_Code(String districtCode) {
        return wardRepository.findByDistrict_Code(districtCode)
                .stream()
                .map(d -> new WardResponse(d.getCode(), d.getName()))
                .toList();
    }
}
