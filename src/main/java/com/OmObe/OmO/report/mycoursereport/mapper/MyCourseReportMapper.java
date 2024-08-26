package com.OmObe.OmO.report.mycoursereport.mapper;

import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.mycoursereport.entity.MyCourseReport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MyCourseReportMapper {
    // ReportDto.Post -> MyCourseReport
    MyCourseReport reportPostDtoToMyCourseReport(ReportDto.Post post);


    // MyCourseReport -> ReportDto.Response
    ReportDto.Response myCourseToReportResponseDto(MyCourseReport myCourseReport);

    // 신고 내용 목록 조회를 위한 ResponseDto List
    List<ReportDto.Response> reportListToReportResponseDto(List<MyCourseReport> myCourseReports);
}
