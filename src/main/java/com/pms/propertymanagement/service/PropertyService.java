package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;

import java.util.List;

public interface PropertyService {
    List<PropertyResponse> getPropertiesByOwner(User owner);
    void saveProperty(Property property, User owner);
}
