package com.OmObe.OmO.report.reviewreport.controller;

import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.reviewreport.entity.ReviewReport;
import com.OmObe.OmO.report.reviewreport.mapper.ReviewReportMapper;
import com.OmObe.OmO.report.reviewreport.service.ReviewReportService;
import com.OmObe.OmO.response.MultiPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/reviewReport")
@RequiredArgsConstructor
@Validated
public class ReviewReportController {
    private final ReviewReportService reviewReportService;
    private final ReviewReportMapper mapper;

    // 리뷰 신고
    @PostMapping("/{reviewId}")
    public ResponseEntity postReviewReport(@RequestBody @Valid ReportDto.Post post,
                                           @RequestHeader("Authorization") String token,
                                           @PathVariable("reviewId") Long reviewId) {
        ReviewReport reviewReport = reviewReportService.createReviewReport(post, token, reviewId);

        return new ResponseEntity<>(mapper.reviewToReviewResponseDto(reviewReport), HttpStatus.CREATED);
    }

    // 신고 내용 조회(관리자 전용)
    @GetMapping
    public ResponseEntity getReviewReports(@RequestParam @Positive int page,
                                           @RequestParam @Positive int size,
                                           @RequestHeader("Authorization") String token) {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Page<ReviewReport> reviewReports = reviewReportService.getReviewReports(page, size, token);
        List<ReviewReport> reviewReportList = reviewReports.getContent();

        return new ResponseEntity<>(
                new MultiPageResponseDto<>(mapper.reviewReportToReviewReportResponeeList(reviewReportList), reviewReports), HttpStatus.OK);
    }
}
