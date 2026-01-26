package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyServiceImpl implements PropertyService
{
    @Autowired
    private PropertyRepository propertyRepository;

    @Override
    public List<PropertyResponse> getPropertiesByOwner(User owner) {
        List<Property> properties = propertyRepository.findByOwnerUsername(owner.getUsername());

        return properties.stream()
                .map(p -> {
                    PropertyResponse dto = new PropertyResponse();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setAddressNumber(p.getAddressNumber());
                    dto.setCategoryName(p.getCategory().getName());

                    dto.setFormattedCreatedAt(
                            DateUtil.formatDateTime(p.getCreatedAt())
                    );

                    return dto;
                })
                .toList();
    }

    @Override
    public void saveProperty(Property property, User owner) {

    }
}
