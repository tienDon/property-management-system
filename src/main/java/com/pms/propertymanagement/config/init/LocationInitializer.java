package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Province;
import com.pms.propertymanagement.entity.Ward;
import com.pms.propertymanagement.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationInitializer {

    private final ProvinceRepository provinceRepository;

    public void init() {
        if (!provinceRepository.findAll().isEmpty()) return;

        Province hcm = new Province("79", "Thành phố Hồ Chí Minh");

        Ward ward = new Ward("27610", "Phường Phước Long A", hcm);
        hcm.getWards().add(ward);

        provinceRepository.save(hcm);
    }
}

