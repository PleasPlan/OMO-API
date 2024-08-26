package com.OmObe.OmO.report.boardreport.controller;

import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.report.ReportDto;
import com.OmObe.OmO.report.boardreport.dto.BoardReportDto;
import com.OmObe.OmO.report.boardreport.entity.BoardReport;
import com.OmObe.OmO.report.boardreport.mapper.BoardReportMapper;
import com.OmObe.OmO.report.boardreport.service.BoardReportService;
import com.OmObe.OmO.response.MultiPageResponseDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/boardReport")
@RequiredArgsConstructor
@Validated
public class BoardReportController {
    private final BoardReportService boardReportService;
    private final BoardReportMapper mapper;

    // 게시글 신고
    @PostMapping("/{boardId}")
    public ResponseEntity postBoardReport(@RequestBody @Valid ReportDto.Post post,
                                          @RequestHeader("Authorization") String token,
                                          @PathVariable("boardId") Long boardId) {
        BoardReport boardReport = boardReportService.createBoardReport(post, token, boardId);

        return new ResponseEntity<>(mapper.boardReportToBoardResponseDto(boardReport), HttpStatus.CREATED);
    }

    // 신고 내용 조회(관리자 전용)
    @GetMapping
    public ResponseEntity getBoardReports(@RequestParam @Positive int page,
                                          @RequestParam @Positive int size,
                                          @RequestHeader("Authorization") String token) {
        Page<BoardReport> boardReports = boardReportService.getBoardReports(page, size, token);
        List<BoardReport> boardReportList = boardReports.getContent();

        return new ResponseEntity<>(
                new MultiPageResponseDto<>(mapper.boardReportToBoardReportResponseList(boardReportList), boardReports), HttpStatus.OK
        );
    }

}
