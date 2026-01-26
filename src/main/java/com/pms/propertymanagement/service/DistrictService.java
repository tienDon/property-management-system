package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.DistrictResponse;

import java.util.List;

public interface DistrictService {

    List<DistrictResponse> findByProvince_Code(String provinceCode);
}
