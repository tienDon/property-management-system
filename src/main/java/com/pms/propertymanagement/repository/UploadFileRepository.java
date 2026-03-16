package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    Optional<UploadFile> findByHash(String hash);
    void deleteByHash(String hash);
}
