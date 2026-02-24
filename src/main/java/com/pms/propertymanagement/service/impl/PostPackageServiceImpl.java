package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.repository.PostingPackageRepository;
import com.pms.propertymanagement.service.PostPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostPackageServiceImpl implements PostPackageService {

    private final PostingPackageRepository postingPackageRepository;

    @Override
    public PostingPackage getDefaultPackage() {
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

    @Override
    public List<PostingPackage> findAllActive() {
        return postingPackageRepository.findAllByOrderByPriceAsc();
    }

    @Override
    public List<PostingPackage> findAll() {
        return postingPackageRepository.findAllByOrderByPriceAsc();
    }
    
    @Override
    public List<PostPackageDTO> getAllPackages() {
        return findAllActive().stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    private PostPackageDTO convertToDTO(PostingPackage entity) {
        PostPackageDTO dto = new PostPackageDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        
        // NEW ARCHITECTURE: Get all data from entity (no hardcoding)
        dto.setDurationDays(entity.getUsageLimit()); // usageLimit = days for post packages
        dto.setFreeBoosts(entity.getFreeBoosts());
        dto.setHasVipBadge(entity.isHasVipBadge());
        dto.setHasSearchPriority(entity.isHasSearchPriority());
        dto.setIsActive(entity.isActive());
        dto.setIsRecommended(entity.isRecommended());
        
        return dto;
    }

    @Override
    public PostingPackage getRecommendedPackage() {
        return postingPackageRepository.findByCode("POST_STANDARD")
                .orElse(getDefaultPackage());
    }
}