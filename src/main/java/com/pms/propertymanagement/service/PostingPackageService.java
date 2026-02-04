package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.PostingPackage;

public interface PostingPackageService {

    PostingPackage getDefaultPostingPackage(); // POST_NEW

    PostingPackage getByCode(String code);

    PostingPackage getById(Long id);
}
