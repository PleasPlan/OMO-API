package com.OmObe.OmO.report.mycoursereport.repository;

import com.OmObe.OmO.report.mycoursereport.entity.MyCourseReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyCourseReportRepository extends JpaRepository<MyCourseReport, Long> {
}
