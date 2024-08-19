package com.OmObe.OmO.report.reviewreport.mapper;

import com.OmObe.OmO.Review.entity.Review;
import com.OmObe.OmO.report.reviewreport.dto.ReviewReportDto;
import com.OmObe.OmO.report.reviewreport.entity.ReviewReport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewReportMapper {
    // ReviewReportDto.Post -> ReviewReport
    ReviewReport reviewReportPostDtoToReviewReport(ReviewReportDto.Post post);

    // Review -> ReviewReportDto.Response
    ReviewReportDto.Response reviewToReviewResponseDto(ReviewReport reviewReport);

    // 신고 내용 목록 조회를 위한 ResponseDto List
    List<ReviewReportDto.Response> reviewReportToReviewReportResponeeList(List<ReviewReport> reviewReports);
}
