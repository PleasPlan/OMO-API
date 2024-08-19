package com.OmObe.OmO.report.reviewreport.repository;

import com.OmObe.OmO.report.reviewreport.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
}
