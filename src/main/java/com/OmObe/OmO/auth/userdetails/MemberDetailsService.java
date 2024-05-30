package com.OmObe.OmO.auth.userdetails;

import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

// db에서 조회한 user의 인증 정보를 기반으로 인증 처리
//@Component
//public class MemberDetailsService implements UserDetailsService {
//    private final MemberRepository memberRepository;
//    private final MemberAuthorityUtils authorityUtils;
//
//    public MemberDetailsService(MemberRepository memberRepository, MemberAuthorityUtils authorityUtils) {
//        this.memberRepository = memberRepository;
//        this.authorityUtils = authorityUtils;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        // 이메일을 통해 회원 조회
//        Optional<Member> optionalMember = memberRepository.findByEmail(username);
//        Member findMember = optionalMember.orElseThrow(() ->
//                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
//
//        return new MemberDetails(findMember);
//    }
//
//    private final class MemberDetails extends Member implements UserDetails {
//        MemberDetails(Member member) {
//            setMemberId(member.getMemberId());
//            setBirth(member.getBirth());
//            setClause(member.getClause());
//            setEmail(member.getEmail());
//            setGender(member.getGender());
//            setMbti(member.getMbti());
//            setMemberStatus(member.getMemberStatus());
//            setNickname(member.getNickname());
//            setPassword(member.getPassword());
//            setMemberRole(member.getMemberRole());
//        }
//
//        @Override
//        public Collection<? extends GrantedAuthority> getAuthorities() {
//            return authorityUtils.createAuthorities(this.getMemberRole());
//        }
//
//        @Override
//        public String getUsername() {
//            return getEmail();
//        }
//
//        @Override
//        public boolean isAccountNonExpired() {
//            return true;
//        }
//
//        @Override
//        public boolean isAccountNonLocked() {
//            return true;
//        }
//
//        @Override
//        public boolean isCredentialsNonExpired() {
//            return true;
//        }
//
//        @Override
//        public boolean isEnabled() {
//            return true;
//        }
//    }
//}
