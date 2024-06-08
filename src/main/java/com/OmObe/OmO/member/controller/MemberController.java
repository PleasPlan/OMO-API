package com.OmObe.OmO.member.controller;

import com.OmObe.OmO.member.dto.MemberDto;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.mapper.MemberMapper;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.member.service.MemberService;
import com.OmObe.OmO.response.ResponseDto;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@Validated
public class MemberController {
    private final MemberMapper mapper;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(MemberMapper mapper, MemberService memberService, MemberRepository memberRepository) {
        this.mapper = mapper;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    // 회원 가입
//    @PostMapping("/signup")
//    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post post){
////        Member member = mapper.memberPostDtoToMember(post);
//        memberService.createMember(post);
//
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }

    // 회원 추가 정보 입력
    @PostMapping("/memberInfo/{memberId}")
    public ResponseEntity addMemberInfo(@Valid @PathVariable("memberId") @Positive Long memberId,
                                        @Valid @RequestBody MemberDto.Post post) {
        memberService.addInfo(memberId, post);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 회원 탈퇴
    @DeleteMapping("/member/{memberId}")
    public ResponseEntity deleteMember(@Valid @PathVariable("memberId") @Positive Long memberId){
        memberService.quitMember(memberId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 닉네임 중복 검증
    @PostMapping("/checkNickname")
    public ResponseEntity checkNickname(@Valid @RequestBody MemberDto.NicknamePatch nickname){
        // NicknamePatch를 통해 중복 여부를 검증할 닉네임을 받아온다.

        // 닉네임 중복 검증 메서드 실행
        memberService.verifyExistsNickname(nickname.getNickname());

        // 닉네임 중복 검증 성공 시 상태 코드와 상태 메시지 설정
        ResponseDto.Response response = new ResponseDto.Response(200, "사용 가능한 닉네임입니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
