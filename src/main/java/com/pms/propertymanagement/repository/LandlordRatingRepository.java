package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.LandlordRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LandlordRatingRepository extends JpaRepository<LandlordRating, Long> {
    Optional<LandlordRating> findByLandlordId(Long landlordId);
}
