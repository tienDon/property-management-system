package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    // Tìm tất cả dịch vụ thuộc các nhà trọ của một chủ sở hữu cụ thể
    List<ServiceItem> findByProperty_Owner_Username(String username);
    
    List<ServiceItem> findByProperty_Id(Long propertyId);
}
