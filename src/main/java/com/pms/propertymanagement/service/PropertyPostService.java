package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.PropertyPost;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface PropertyPostService {

    PropertyPost createPost(Property property, PostingPackage postPackage);
    
    PropertyPost renewPost(PropertyPost post, PostingPackage postPackage);
    
    void extendPost(PropertyPost post, int additionalDays);
    
    PropertyPost findByProperty(Property property);
    
    Optional<PropertyPost> findByPropertyId(Long propertyId);
    
    List<PropertyPost> findActivePostsByOwner(User owner);
    
    List<PropertyPost> findExpiringPosts(int daysAhead);
    
    List<PropertyPost> findExpiredPosts();
    
    void expirePost(PropertyPost post);
    
    void incrementViewCount(PropertyPost post);
    
    void incrementContactCount(PropertyPost post);
}