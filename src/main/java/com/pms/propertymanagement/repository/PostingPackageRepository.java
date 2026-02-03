package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PostingPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostingPackageRepository extends JpaRepository<PostingPackage, Long> {
    Optional<PostingPackage> findByCode(String code);
}