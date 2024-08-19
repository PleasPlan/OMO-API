package com.OmObe.OmO.report.reviewreport.controller;

import com.OmObe.OmO.report.reviewreport.dto.ReviewReportDto;
import com.OmObe.OmO.report.reviewreport.entity.ReviewReport;
import com.OmObe.OmO.report.reviewreport.mapper.ReviewReportMapper;
import com.OmObe.OmO.report.reviewreport.service.ReviewReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/reviewReport")
@RequiredArgsConstructor
@Validated
public class ReviewReportController {
    private final ReviewReportService reviewReportService;
    private final ReviewReportMapper mapper;

    // 리뷰 신고
    @PostMapping("/{reviewId}")
    public ResponseEntity postReviewReport(@RequestBody @Valid ReviewReportDto.Post post,
                                           @RequestHeader("Authorization") String token,
                                           @PathVariable("reviewId") Long reviewId) {
        ReviewReport reviewReport = reviewReportService.createReviewReport(post, token, reviewId);

        return new ResponseEntity<>(mapper.reviewToReviewResponseDto(reviewReport), HttpStatus.CREATED);
    }
}
