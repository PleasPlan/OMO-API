package com.OmObe.OmO.notice.service;

import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.member.service.MemberService;
import com.OmObe.OmO.notice.dto.NoticeDto;
import com.OmObe.OmO.notice.entity.Notice;
import com.OmObe.OmO.notice.mapper.NoticeMapper;
import com.OmObe.OmO.notice.repository.NoticeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    private final TokenDecryption tokenDecryption;
    private final NoticeMapper mapper;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    /**
     * <공지사항 등록>
     * 1. 회원 매핑
     * 2. 작성자 검증
     * 3. 공지사항 저장
     */
    public Notice createNotice(NoticeDto.Post post, String token) throws JsonProcessingException {
        Notice notice = mapper.noticePostDtoToNotice(post);
        // TODO : 작성 시간을 setter로 선언해야 정상적으로 db에 작성 시간이 저장됨 <수정 필요>
        notice.setCreatedAt(LocalDateTime.now());

        // 1. 회원 매핑
        try {
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            notice.setMember(member);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }

        // 2. 작성자 검증
//        memberService.verifiedAuthenticatedMember(notice.getMember().getMemberId());

        // 3. 공자사항 저장
        return noticeRepository.save(notice);
    }

    /**
     * <공지사항 수정>
     * 1. 게시글 존재 여부 검증
     * 2. 게시글 작성자와 수정자 비교
     * 3. 수정
     */
    public Notice patchNotice(NoticeDto.Patch patch, Long noticeId, String token) {
        patch.setNoticeId(noticeId);
        Notice notice = mapper.noticePatchDtoToNotice(patch);

        // 1. 게시글 존재 여부 검증
        Notice checkedNotice = verifyNotice(notice.getNoticeId());
        checkedNotice.setModifiedAt(LocalDateTime.now());

        // 2. 게시글 작성자와 수정자 비교
        try {
            Member member = tokenDecryption.getWriterInJWTToken(token);
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            memberService.verifiedAuthenticatedMember(member.getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }

        // 3. 수정
        Optional.ofNullable(notice.getTitle()) // 제목
                .ifPresent(checkedNotice::setTitle);

        Optional.ofNullable(notice.getContent()) // 내용
                .ifPresent(checkedNotice::setContent);

        Optional.ofNullable(notice.getType()) // 공지 구분
                .ifPresent(checkedNotice::setType);

        return noticeRepository.save(checkedNotice);
    }

    /**
     * <공지사항 목록 조회 - 기본 조회>
     * 1. 최신순으로 조회 및 리턴
     * 메서드 타입이 Page인 경우 전체 데이터 및 페이지 수 계산을 위한 count 쿼리를 추가로 호출하는데 필요 없기 때문에
     * 다음 Slice가 있는지만 판단하는 Slice타입으로 변경하여 필요하지 않은 쿼리의 호출을 줄임
     */
    public Slice<Notice> getSortedNotice(int page, int size) {
        // 1. 최신순으로 조회
        return noticeRepository.findAll(sortedBy(page, size));
    }

    /**
     * <공지사항 목록 조회 - 일반 공지 / 점검 공지>
     * 1. NOR, CHK 중 입력받은 타입에 따라 해당 타입의 공지사항 목록을 최신순으로 제공
     */
    public Slice<Notice> getNoticesByType(int page, int size, String type) {
        // 1. NOR, CHK 중 입력받은 타입에 따라 해당 타입의 공지사항 목록을 최신순으로 제공
        return noticeRepository.findByType(type, sortedBy(page, size));
    }

    /**
     * <공지사항 삭제>
     * 1. 사용자 로그인 상태 검증
     * 2. 삭제
     */
    public void removeNotice(Long noticeId, String token) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.NOTICE_NOT_FOUND));

        // 1. 사용자 로그인 상태 검증
        try {
            Member member = tokenDecryption.getWriterInJWTToken(token);
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            memberService.verifiedAuthenticatedMember(member.getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }


        // 2. 삭제
        noticeRepository.delete(notice);
    }

    // 공지사항을 최신순으로 정렬
    private Pageable sortedBy(int page, int size) {
        return PageRequest.of(page - 1, size, Sort.by("noticeId").descending());
    }

    // 게시글 존재 검증 메서드
    private Notice verifyNotice(Long noticeId) {
        Optional<Notice> optionalNotice = noticeRepository.findById(noticeId);

        return optionalNotice.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.NOTICE_NOT_FOUND));
    }

    public static Slice<Notice> convertToSlice(Page<Notice> page){
        return new SliceImpl<>(page.getContent(), page.getPageable(), page.hasNext());
    }
}
