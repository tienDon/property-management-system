package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District,String> {
    List<District> findByProvince_Code(String provinceCode);
}
