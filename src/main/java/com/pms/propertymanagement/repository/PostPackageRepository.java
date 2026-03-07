package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PostPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostPackageRepository extends JpaRepository<PostPackage, Long> {
    
    Optional<PostPackage> findByCode(String code);
        List<PostPackage> findByActiveTrueOrderBySortOrderAsc();
    
    List<PostPackage> findAllByOrderBySortOrderAsc();
        @Query("SELECT pp FROM PostPackage pp WHERE pp.isActive = true ORDER BY pp.sortOrder")
    List<PostPackage> findAllActiveOrderBySortOrder();
    
    @Query("SELECT pp FROM PostPackage pp WHERE pp.isActive = true AND pp.durationDays >= :minDays ORDER BY pp.price")
    List<PostPackage> findByMinDurationOrderByPrice(@Param("minDays") int minDays);
    
    @Query("SELECT pp FROM PostPackage pp WHERE pp.isActive = true AND pp.price BETWEEN :minPrice AND :maxPrice ORDER BY pp.durationDays")
    List<PostPackage> findByPriceRangeOrderByDuration(@Param("minPrice") int minPrice, @Param("maxPrice") int maxPrice);
}