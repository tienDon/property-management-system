package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.entity.PostingPackage;

import java.util.List;

public interface PostingPackageService {

    PostingPackage getDefaultPostingPackage(); // POST_NEW

    /** Alias for getDefaultPostingPackage() */
    PostingPackage getDefaultPackage();

    PostingPackage getByCode(String code);

    PostingPackage getById(Long id);

    List<PostingPackage> findAllActive();

    List<PostingPackage> findAll();

    List<PostPackageDTO> getAllPackages();

    PostingPackage getRecommendedPackage();
}
