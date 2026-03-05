package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Property;

public interface PropertyManagementService {

    record PropertyCreationStatus(
            boolean canCreate,
            String message,
            int currentActiveProperties,
            int maxAllowedProperties) {}

    boolean canCreateProperty(Long userId);

    void handleNewPropertyCreation(Property property, String postTitle, String postSlug, String postDescription);

    PropertyCreationStatus getPropertyCreationStatus(Long userId);

    boolean canPostToMarketplace(Long userId, Long propertyId);
}
