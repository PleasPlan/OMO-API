package com.OmObe.OmO.report.mycoursereport.controller;

import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.mycoursereport.entity.MyCourseReport;
import com.OmObe.OmO.report.mycoursereport.mapper.MyCourseReportMapper;
import com.OmObe.OmO.report.mycoursereport.service.MyCourseReportService;
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
@Validated
@RequiredArgsConstructor
@RequestMapping("/myCourseReport")
public class MyCourseReportController {
    private final MyCourseReportService myCourseReportService;
    private final MyCourseReportMapper mapper;

    // 나만의 코스 신고
    @PostMapping("/{myCourseId}")
    public ResponseEntity postMyCourseReport(@RequestBody @Valid ReportDto.Post post,
                                             @RequestHeader("Authorization") String token,
                                             @PathVariable("myCourseId") Long myCourseId) {
        MyCourseReport myCourseReport = myCourseReportService.createMyCourseReport(post, token, myCourseId);

        return new ResponseEntity<>(mapper.myCourseToReportResponseDto(myCourseReport), HttpStatus.CREATED);
    }

    // 신고 내용 조회(관리자 전용)
    @GetMapping
    public ResponseEntity getMyCourseReports(@RequestParam @Positive int page,
                                             @RequestParam @Positive int size,
                                             @RequestHeader("Authorization") String token) {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Page<MyCourseReport> myCourseReports = myCourseReportService.getMyCourseReports(page, size, token);
        List<MyCourseReport> myCourseReportList = myCourseReports.getContent();

        return new ResponseEntity<>(
                new MultiPageResponseDto<>(mapper.reportListToReportResponseDto(myCourseReportList), myCourseReports), HttpStatus.OK
        );
    }
}
