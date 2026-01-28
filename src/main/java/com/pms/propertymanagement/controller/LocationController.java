package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.Ward;
import com.pms.propertymanagement.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/wards/{provinceCode}")
    public ResponseEntity<List<Ward>> getWardsByProvince(@PathVariable String provinceCode) {
        List<Ward> wards = locationService.getWardsByProvince(provinceCode);
        return ResponseEntity.ok(wards);
    }
}
