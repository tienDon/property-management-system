package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Ward;

import java.util.List;

public interface LocationService {
    List<Ward> getWardsByProvince(String provinceCode);
}
