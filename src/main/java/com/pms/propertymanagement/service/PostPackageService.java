package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.entity.PostingPackage;

import java.util.List;

public interface PostPackageService {

    PostingPackage getDefaultPackage(); // POST_NEW
    
    PostingPackage getByCode(String code);
    
    PostingPackage getById(Long id);
    
    List<PostingPackage> findAllActive();
    
    List<PostingPackage> findAll();
    
    List<PostPackageDTO> getAllPackages();
    
    PostingPackage getRecommendedPackage();
}