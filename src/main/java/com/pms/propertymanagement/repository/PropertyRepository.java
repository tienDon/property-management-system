package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property,Long> {
    List<Property> findByOwnerUsername(String ownerUsername);
}
