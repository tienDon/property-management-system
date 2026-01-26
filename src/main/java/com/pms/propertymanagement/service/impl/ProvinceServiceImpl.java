package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Province;
import com.pms.propertymanagement.repository.ProvinceRepository;
import com.pms.propertymanagement.service.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinceServiceImpl implements ProvinceService {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Override
    public List<Province> findAllProvince() {
        return provinceRepository.findAll();
    }
}
