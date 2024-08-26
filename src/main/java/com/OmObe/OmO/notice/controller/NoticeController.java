package com.OmObe.OmO.notice.controller;

import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.notice.dto.NoticeDto;
import com.OmObe.OmO.notice.entity.Notice;
import com.OmObe.OmO.notice.mapper.NoticeMapper;
import com.OmObe.OmO.notice.service.NoticeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Validated
public class NoticeController {
    private final NoticeService noticeService;
    private final NoticeMapper mapper;

    // 공지사항 작성
    @PostMapping("/write")
    public ResponseEntity postNotice(@RequestBody @Valid NoticeDto.Post post, @RequestHeader("Authorization") String token) throws JsonProcessingException {
        Notice notice = noticeService.createNotice(post, token);

        return new ResponseEntity<>(mapper.noticeToNoticeResponseDto(notice), HttpStatus.CREATED);
    }

    // 공지사항 수정
    @PatchMapping("/{noticeId}")
    public ResponseEntity patchNotice(@RequestBody @Valid NoticeDto.Patch patch,
                                      @PathVariable("noticeId") Long noticeId,
                                      @RequestHeader("Authorization") String token) {
        Notice notice = noticeService.patchNotice(patch, noticeId, token);

        return new ResponseEntity<>(mapper.noticeToNoticeResponseDto(notice), HttpStatus.OK);
    }

    // 공지사항 조회
    @GetMapping()
    public ResponseEntity getNotices(@RequestParam int page,
                                     @RequestParam @Positive int size,
                                     @RequestParam String type) {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Slice<Notice> sortedNotices;
        if (type.equals("all")) { // 기본값은 최신순으로 조회
            sortedNotices = noticeService.getSortedNotice(page, size);
        } else if (type.equals("CHK") || type.equals("NOR")) { // CHK나 NOR인 경우 해당 타입에 맞게 분류하여 공지사항 목록 제공
            sortedNotices = noticeService.getNoticesByType(page, size, type);
        } else { // default, CHK, NOR 이외의 타입이 입력된 경우 예외 처리
            throw new BusinessLogicException(ExceptionCode.NOTICE_TYPE_ERROR);
        }

        List<Notice> notices = sortedNotices.getContent();

        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.noticeToNoticeResponseList(notices), sortedNotices), HttpStatus.OK);
    }

    // 공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity deleteNotice(@PathVariable("noticeId") Long noticeId,
                                       @RequestHeader("Authorization") String token) {
        noticeService.removeNotice(noticeId, token);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
