package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.request.PropertyRequest;
import com.pms.propertymanagement.dto.response.PropertyDetailResponse;
import com.pms.propertymanagement.dto.response.PropertyOwnerResponse;
import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;

import java.util.List;

import com.pms.propertymanagement.entity.Amenity;
import com.pms.propertymanagement.entity.Province;
import com.pms.propertymanagement.entity.Surrounding;
import com.pms.propertymanagement.entity.TargetTenant;
// ... imports

public interface PropertyService {
    List<PropertyOwnerResponse> getPropertiesByOwner(User owner);
    void createProperty(PropertyRequest request, User owner);

    List<PropertyResponse> getPropertiesByCategory(Long categoryId);

    PropertyDetailResponse getPropertyDetailBySlug(String slug);

    // Edit/Delete for Owner
    PropertyRequest getPropertyForEdit(Long id);
    void updateProperty(Long id, PropertyRequest request);
    void deleteProperty(Long id);

    // Reference Data Methods
    List<Amenity> getAllAmenities();
    List<Surrounding> getAllSurroundings();
    List<TargetTenant> getAllTargetTenants();
    List<Province> getAllProvinces();
}
