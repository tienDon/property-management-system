package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.ReviewReport;
import com.pms.propertymanagement.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    List<ReviewReport> findByStatus(ReportStatus status);
}
