package com.OmObe.OmO.member.controller;

import com.OmObe.OmO.member.dto.MemberDto;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.mapper.MemberMapper;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
    @PostMapping("/signup")
    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post post) throws Exception {
        Member member = mapper.memberPostDtoToMember(post);
        memberService.createMember(member);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
