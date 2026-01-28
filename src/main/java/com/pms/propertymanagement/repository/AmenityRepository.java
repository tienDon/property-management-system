package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity,Long> {
}
