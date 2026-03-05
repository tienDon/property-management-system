package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PropertySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Property Slots
 */
@Repository
public interface PropertySlotRepository extends JpaRepository<PropertySlot, Long> {
    
    Optional<PropertySlot> findByPropertyId(Long propertyId);
    
    @Query("SELECT ps FROM PropertySlot ps WHERE ps.ownerId = :ownerId")
    List<PropertySlot> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT ps FROM PropertySlot ps WHERE ps.ownerId = :ownerId AND ps.isActive = true")
    List<PropertySlot> findActiveSlotsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT COUNT(ps) FROM PropertySlot ps WHERE ps.ownerId = :ownerId AND ps.isActive = true")
    Integer countActiveSlotsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT ps FROM PropertySlot ps WHERE ps.ownerId = :ownerId AND ps.isActive = false")
    List<PropertySlot> findInactiveSlotsByOwner(@Param("ownerId") Long ownerId);
    
    boolean existsByPropertyIdAndIsActive(Long propertyId, Boolean isActive);
}