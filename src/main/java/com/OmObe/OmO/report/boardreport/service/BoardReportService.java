package com.OmObe.OmO.report.boardreport.service;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.service.BoardService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.service.MemberService;
import com.OmObe.OmO.report.boardreport.dto.BoardReportDto;
import com.OmObe.OmO.report.boardreport.entity.BoardReport;
import com.OmObe.OmO.report.boardreport.mapper.BoardReportMapper;
import com.OmObe.OmO.report.boardreport.repository.BoardReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BoardReportService {
    private final BoardReportRepository boardReportRepository;
    private final BoardReportMapper mapper;
    private final BoardService boardService;
    private final TokenDecryption tokenDecryption;
    private final MemberService memberService;


    /**
     * <게시글 신고>
     * 1. 게시글 검증(존재하는 게시글?)
     * 2. 신고 유형이 6(기타)인 경우 신고 사유 유무 파악
     * 3. 인증된 사용자와 게시글을 boardReport 객체에 저장
     * 4. 신고 내용 저장
     */
    public BoardReport createBoardReport(BoardReportDto.Post post, String token, Long boardId) {

        // 1. 게시글 검증(존재하는 게시글?)
        Board board = boardService.findBoard(boardId);

        // 2. 신고 유형이 6(기타)인 경우 신고 사유 유무 파악
        if (post.getReportType() == 6) {
            if (post.getReason().isBlank()) { // 신고 유형이 6인데 신고 사유가 없는 경우 예외처리
                throw new BusinessLogicException(ExceptionCode.REPORT_REASON_NOT_EXIST);
            }
        }
//
        // 3. 인증된 사용자와 게시글을 boardReport 객체에 저장
        BoardReport boardReport = mapper.boardReportPostDtoToBoardReport(post);
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            boardReport.setMember(member);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }
        boardReport.setBoard(board);

        // 4. 신고 내용 저장
        return boardReportRepository.save(boardReport);
    }

    /**
     * <신고 내용 조회 - 관리자 전용>
     * 1. 토큰 검증
     * 2. 먼저 신고된 순으로 정렬(과거순)
     */
    public Page<BoardReport> getBoardReports(int page, int size, String token) {
        // 1. 토큰 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
        } catch (JsonProcessingException je) {
            throw new RuntimeException(je);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 2. 먼저 신고된 순으로 정렬(과거순)
        return boardReportRepository.findAll(reportSortedBy(page, size));
    }

    // 신고 내용 목록을 과거순으로 조회
    private Pageable reportSortedBy(int page, int size) {
        return PageRequest.of(page - 1, size, Sort.by("boardReportId").ascending());
    }
}