package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.PropertyCommentReport;
import com.pms.propertymanagement.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyCommentReportRepository extends JpaRepository<PropertyCommentReport, Long> {
    List<PropertyCommentReport> findByStatus(ReportStatus status);
}
