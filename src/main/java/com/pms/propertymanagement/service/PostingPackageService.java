package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.PostingPackage;

import java.util.List;

public interface PostingPackageService {

    PostingPackage getDefaultPostingPackage(); // POST_NEW

    PostingPackage getByCode(String code);

    PostingPackage getById(Long id);
    
    List<PostingPackage> findAll();
}
