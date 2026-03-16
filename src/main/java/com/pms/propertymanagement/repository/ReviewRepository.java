package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Review;
import com.pms.propertymanagement.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPropertyIdAndStatus(Long propertyId, ReviewStatus status);
    List<Review> findByLandlordIdAndStatus(Long landlordId, ReviewStatus status);
    List<Review> findByStatus(ReviewStatus status);
    Optional<Review> findByContractIdAndTenantId(Long contractId, Long tenantId);
    
    List<Review> findByLandlordId(Long landlordId);
}
