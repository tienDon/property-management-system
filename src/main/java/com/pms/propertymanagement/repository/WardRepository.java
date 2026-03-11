package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward,String> {
    List<Ward> findByProvince_Code(String provinceCode);

    //hỗ trợ filter trên search
    List<Ward> findByNameIgnoreCaseAndProvince_Code(String name,String provinceCode);

    /**
     * Search wards by partial name within a specific province (for chat location resolution).
     * Case-insensitive LIKE search, restricted to the given provinceCode to avoid cross-province ambiguity.
     */
    @Query("SELECT w FROM Ward w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND w.province.code = :provinceCode")
    List<Ward> searchByNameInProvince(@Param("keyword") String keyword,
                                      @Param("provinceCode") String provinceCode);
}
