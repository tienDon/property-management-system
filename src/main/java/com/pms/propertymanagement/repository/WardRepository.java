package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward,String> {
    List<Ward> findByProvince_Code(String provinceCode);

    //hỗ trợ filter trên search
    List<Ward> findByNameIgnoreCaseAndProvince_Code(String name,String provinceCode);
}
