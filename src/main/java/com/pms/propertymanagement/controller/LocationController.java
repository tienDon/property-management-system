package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.response.DistrictResponse;
import com.pms.propertymanagement.dto.response.WardResponse;
import com.pms.propertymanagement.service.DistrictService;
import com.pms.propertymanagement.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final DistrictService districtService;
    private final WardService wardService;

    // ✅ /api/locations/districts?provinceId=01
    @GetMapping("/districts")
    public List<DistrictResponse> getDistrictsByProvince(
            @RequestParam String provinceId
    ) {
        return districtService.findByProvince_Code(provinceId);
    }

    // ✅ /api/locations/wards?districtId=001
    @GetMapping("/wards")
    public List<WardResponse> getWardsByDistrict(
            @RequestParam String districtId
    ) {
        return wardService.findByDistrict_Code(districtId);
    }
}
