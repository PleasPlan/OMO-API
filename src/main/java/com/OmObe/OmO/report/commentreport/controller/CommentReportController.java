package com.OmObe.OmO.report.commentreport.controller;

import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.commentreport.entity.CommentReport;
import com.OmObe.OmO.report.commentreport.mapper.CommentReportMapper;
import com.OmObe.OmO.report.commentreport.service.CommentReportService;
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
@RequestMapping("/commentReport")
public class CommentReportController {
    private final CommentReportService commentReportService;
    private final CommentReportMapper mapper;

    // 댓글 신고
    @PostMapping("/{commentId}")
    public ResponseEntity postCommentReport(@RequestBody @Valid ReportDto.Post post,
                                            @RequestHeader("Authorization") String token,
                                            @PathVariable("commentId") Long commentId) {
        CommentReport commentReport = commentReportService.createCommentReport(post, token, commentId);

        return new ResponseEntity<>(mapper.commentReportToCommentResponseDto(commentReport), HttpStatus.CREATED);
    }

    // 댓글 조회(관리자 전용)
    @GetMapping
    public ResponseEntity getCommentReports(@RequestParam @Positive int page,
                                            @RequestParam @Positive int size,
                                            @RequestHeader("Authorization") String token) {
        Page<CommentReport> commentReports = commentReportService.getCommentReports(page, size, token);
        List<CommentReport> commentReportList = commentReports.getContent();

        return new ResponseEntity<>(
                new MultiPageResponseDto<>(mapper.commentReportListToCommentResponseDtoList(commentReportList), commentReports),
                HttpStatus.OK
        );
    }
}
