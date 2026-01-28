package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Ward;
import com.pms.propertymanagement.repository.WardRepository;
import com.pms.propertymanagement.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final WardRepository wardRepository;

    @Override
    public List<Ward> getWardsByProvince(String provinceCode) {
        return wardRepository.findByProvince_Code(provinceCode);
    }
}
