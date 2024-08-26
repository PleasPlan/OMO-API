package com.OmObe.OmO.report.commentreport.mapper;

import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.commentreport.entity.CommentReport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentReportMapper {
    // CommentReportDto.Post -> CommentReport
    CommentReport commentReportPostDtoToCommentReport(ReportDto.Post post);

    // CommentReport -> CommentReportDto.Response
    ReportDto.Response commentReportToCommentResponseDto(CommentReport commentReport);

    // 신고 내용 목록 조회를 위한 ResponseDto List
    List<ReportDto.Response> commentReportListToCommentResponseDtoList(List<CommentReport> commentReports);
}
