package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.dto.response.RoomSearchResponse;
import com.pms.propertymanagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {


    //Lấy các phòng is_deleted = false
    List<Room> findByIsDeletedFalse();

    //Lấy các phòng is_deleted = false và status = AVAILABLE
    List<Room> findByIsDeletedFalseAndStatus(String status);


    @Query("""
        SELECT r FROM Room r
        WHERE r.isDeleted = false
        
        AND (:categoryId IS NULL OR r.category.id = :categoryId)

        AND (:provinceCode IS NULL OR :provinceCode = '' OR r.property.ward.district.province.code = :provinceCode)
        AND (:districtCode IS NULL OR :districtCode = '' OR r.property.ward.district.code = :districtCode)
        AND (:wardCode IS NULL OR :wardCode = '' OR r.property.ward.code = :wardCode)

        AND (:minPrice IS NULL OR r.price >= :minPrice)
        AND (:maxPrice IS NULL OR r.price <= :maxPrice)

        AND (:minArea IS NULL OR r.area >= :minArea)
        AND (:maxArea IS NULL OR r.area <= :maxArea)
    """)
    List<Room> searchRooms(
            @Param("provinceCode") String provinceCode,
            @Param("districtCode") String districtCode,
            @Param("wardCode") String wardCode,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minArea") Double minArea,
            @Param("maxArea") Double maxArea
    );
}
