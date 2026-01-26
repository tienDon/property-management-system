package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.WardResponse;

import java.util.List;

public interface WardService {

    List<WardResponse> findByDistrict_Code(String districtCode);
}
