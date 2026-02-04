package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property,Long> {
    List<Property> findByOwnerUsername(String ownerUsername);
    List<Property> findByOwnerId(Long ownerId);;

    List<Property> findByCategory_Id(Long categoryId);

    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.ward w " +
            "LEFT JOIN FETCH w.province " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.amenities " +
            "LEFT JOIN FETCH p.surroundings " +
            "LEFT JOIN FETCH p.targetTenants " +
            "LEFT JOIN FETCH p.owner " +
            "WHERE p.slug = :slug")
    Optional<Property> findBySlugWithDetails(@Param("slug") String slug);

    Optional<Property> findBySlug(String slug);



}
