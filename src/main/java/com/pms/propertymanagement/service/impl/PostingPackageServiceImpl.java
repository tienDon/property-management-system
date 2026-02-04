package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.repository.PostingPackageRepository;
import com.pms.propertymanagement.service.PostingPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostingPackageServiceImpl implements PostingPackageService {

    private final PostingPackageRepository postingPackageRepository;

    @Override
    public PostingPackage getDefaultPostingPackage() {
        return postingPackageRepository.findByCode("POST_NEW")
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói POST_NEW. Hãy kiểm tra initializer."));
    }

    @Override
    public PostingPackage getByCode(String code) {
        return postingPackageRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói: " + code));
    }

    @Override
    public PostingPackage getById(Long id) {
        return postingPackageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy package id=" + id));
    }
}