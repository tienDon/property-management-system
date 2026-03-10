package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.dto.ApiStatisticDTO;
import com.pms.propertymanagement.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {

    @Query("SELECT new com.pms.propertymanagement.dto.ApiStatisticDTO(" +
            "l.apiName, l.path, COUNT(l), " +
            "SUM(CASE WHEN l.statusCode = 200 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN l.statusCode = 400 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN l.statusCode > 400 AND l.statusCode < 500 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN l.statusCode >= 500 THEN 1 ELSE 0 END)) " +
            "FROM ApiLog l " +
            "GROUP BY l.apiName, l.path")
    List<ApiStatisticDTO> getApiStatistics();
}
